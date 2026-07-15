#include <jni.h>
#include <windows.h>

#include <mutex>
#include <string>

#include <winrt/Windows.Foundation.h>
#include <winrt/Windows.Media.Control.h>
#include <winrt/base.h>

using namespace winrt;
using namespace Windows::Media::Control;

namespace {

std::mutex g_mutex;
bool g_apartment_initialized = false;
GlobalSystemMediaTransportControlsSessionManager g_manager{nullptr};
std::string g_last_error;

struct SmtcState {
	bool initialized = false;
	bool has_session = false;
	std::wstring session_id;
	std::wstring application_id;
	std::wstring application_name;
	std::wstring title;
	std::wstring artist;
	std::wstring album_title;
	std::wstring album_artist;
	int playback_status = 3;
	long long position_ms = 0;
	long long duration_ms = 0;
};

SmtcState g_state;

void set_last_error(std::string message) {
	g_last_error = std::move(message);
}

std::wstring to_lower(std::wstring value) {
	for (wchar_t& character : value) {
		character = static_cast<wchar_t>(towlower(character));
	}
	return value;
}

bool is_spotify_app_id(const std::wstring& app_id) {
	return to_lower(app_id).find(L"spotify") != std::wstring::npos;
}

std::wstring hstring_to_wstring(hstring const& value) {
	return std::wstring(value.c_str());
}

long long ticks_to_ms(int64_t ticks) {
	return ticks / 10'000;
}

std::string wide_to_utf8(const std::wstring& wide) {
	if (wide.empty()) {
		return "";
	}

	const int wide_length = static_cast<int>(wide.size());
	int size = WideCharToMultiByte(CP_UTF8, 0, wide.data(), wide_length, nullptr, 0, nullptr, nullptr);
	if (size <= 0) {
		return "";
	}

	std::string result(static_cast<size_t>(size), '\0');
	WideCharToMultiByte(
			CP_UTF8,
			0,
			wide.data(),
			wide_length,
			result.data(),
			size,
			nullptr,
			nullptr
	);
	return result;
}

jstring to_jstring(JNIEnv* env, const std::wstring& wide) {
	if (wide.empty()) {
		return env->NewString(nullptr, 0);
	}

	return env->NewString(
			reinterpret_cast<const jchar*>(wide.data()),
			static_cast<jsize>(wide.size())
	);
}

jstring to_jstring(JNIEnv* env, const std::string& value) {
	if (value.empty()) {
		return env->NewString(nullptr, 0);
	}

	const int byte_length = static_cast<int>(value.size());
	int wide_length = MultiByteToWideChar(CP_UTF8, MB_ERR_INVALID_CHARS, value.data(), byte_length, nullptr, 0);
	if (wide_length <= 0) {
		return env->NewStringUTF(value.c_str());
	}

	std::wstring wide(static_cast<size_t>(wide_length), L'\0');
	MultiByteToWideChar(CP_UTF8, MB_ERR_INVALID_CHARS, value.data(), byte_length, wide.data(), wide_length);
	return to_jstring(env, wide);
}

void reset_state_locked() {
	g_state = {};
	g_state.initialized = g_manager != nullptr;
}

bool synchronize_state_locked() {
	reset_state_locked();

	if (!g_manager) {
		set_last_error("Session manager is not available");
		return false;
	}

	GlobalSystemMediaTransportControlsSession session = g_manager.GetCurrentSession();
	if (!session) {
		set_last_error("No current Windows media session");
		return false;
	}

	const std::wstring application_id = hstring_to_wstring(session.SourceAppUserModelId());
	if (!is_spotify_app_id(application_id)) {
		set_last_error("Current session is not Spotify: " + wide_to_utf8(application_id));
		return false;
	}

	try {
		GlobalSystemMediaTransportControlsSessionMediaProperties properties =
				session.TryGetMediaPropertiesAsync().get();
		GlobalSystemMediaTransportControlsSessionPlaybackInfo playback_info = session.GetPlaybackInfo();
		GlobalSystemMediaTransportControlsSessionTimelineProperties timeline = session.GetTimelineProperties();

		const std::wstring title = hstring_to_wstring(properties.Title());
		if (title.empty()) {
			set_last_error("Spotify session returned empty track title");
			return false;
		}

		g_state.has_session = true;
		g_state.session_id = application_id;
		g_state.application_id = application_id;
		g_state.application_name = L"Spotify";
		g_state.title = title;
		g_state.artist = hstring_to_wstring(properties.Artist());
		g_state.album_title = hstring_to_wstring(properties.AlbumTitle());
		g_state.album_artist = hstring_to_wstring(properties.AlbumArtist());
		g_state.playback_status = static_cast<int>(playback_info.PlaybackStatus());
		g_state.position_ms = ticks_to_ms(timeline.Position().count());
		g_state.duration_ms = ticks_to_ms((timeline.EndTime() - timeline.StartTime()).count());
		set_last_error("");
		return true;
	} catch (const hresult_error& error) {
		set_last_error("WinRT metadata retrieval failed: " + wide_to_utf8(error.message().c_str()));
		return false;
	} catch (...) {
		set_last_error("WinRT metadata retrieval failed with unknown error");
		return false;
	}
}

} // namespace

extern "C" {

JNIEXPORT jboolean JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeInitialize(JNIEnv*, jclass) {
	std::lock_guard lock(g_mutex);

	if (g_state.initialized) {
		set_last_error("");
		return JNI_TRUE;
	}

	try {
		if (!g_apartment_initialized) {
			init_apartment(apartment_type::multi_threaded);
			g_apartment_initialized = true;
		}

		g_manager = GlobalSystemMediaTransportControlsSessionManager::RequestAsync().get();
		g_state.initialized = g_manager != nullptr;
		if (!g_state.initialized) {
			set_last_error("GlobalSystemMediaTransportControlsSessionManager::RequestAsync returned null");
			return JNI_FALSE;
		}

		set_last_error("");
		return JNI_TRUE;
	} catch (const hresult_error& error) {
		g_manager = nullptr;
		g_state = {};
		set_last_error("WinRT initialization failed: " + wide_to_utf8(error.message().c_str()));
		return JNI_FALSE;
	} catch (...) {
		g_manager = nullptr;
		g_state = {};
		set_last_error("WinRT initialization failed with unknown error");
		return JNI_FALSE;
	}
}

JNIEXPORT void JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeShutdown(JNIEnv*, jclass) {
	std::lock_guard lock(g_mutex);
	g_manager = nullptr;
	reset_state_locked();
	set_last_error("");
}

JNIEXPORT jboolean JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeSynchronize(JNIEnv*, jclass) {
	std::lock_guard lock(g_mutex);
	return synchronize_state_locked() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeHasActiveSession(JNIEnv*, jclass) {
	std::lock_guard lock(g_mutex);
	return g_state.has_session ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeGetSessionId(JNIEnv* env, jclass) {
	std::lock_guard lock(g_mutex);
	return to_jstring(env, g_state.session_id);
}

JNIEXPORT jstring JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeGetApplicationId(JNIEnv* env, jclass) {
	std::lock_guard lock(g_mutex);
	return to_jstring(env, g_state.application_id);
}

JNIEXPORT jstring JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeGetApplicationName(JNIEnv* env, jclass) {
	std::lock_guard lock(g_mutex);
	return to_jstring(env, g_state.application_name);
}

JNIEXPORT jstring JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeGetTitle(JNIEnv* env, jclass) {
	std::lock_guard lock(g_mutex);
	return to_jstring(env, g_state.title);
}

JNIEXPORT jstring JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeGetArtist(JNIEnv* env, jclass) {
	std::lock_guard lock(g_mutex);
	return to_jstring(env, g_state.artist);
}

JNIEXPORT jstring JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeGetAlbumTitle(JNIEnv* env, jclass) {
	std::lock_guard lock(g_mutex);
	return to_jstring(env, g_state.album_title);
}

JNIEXPORT jstring JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeGetAlbumArtist(JNIEnv* env, jclass) {
	std::lock_guard lock(g_mutex);
	return to_jstring(env, g_state.album_artist);
}

JNIEXPORT jint JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeGetPlaybackStatus(JNIEnv*, jclass) {
	std::lock_guard lock(g_mutex);
	return static_cast<jint>(g_state.playback_status);
}

JNIEXPORT jlong JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeGetPositionMs(JNIEnv*, jclass) {
	std::lock_guard lock(g_mutex);
	return static_cast<jlong>(g_state.position_ms);
}

JNIEXPORT jlong JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeGetDurationMs(JNIEnv*, jclass) {
	std::lock_guard lock(g_mutex);
	return static_cast<jlong>(g_state.duration_ms);
}

JNIEXPORT jstring JNICALL
Java_dev_cronis_spotify_os_windows_WindowsSmtcNativeJni_nativeGetLastError(JNIEnv* env, jclass) {
	std::lock_guard lock(g_mutex);
	return to_jstring(env, g_last_error);
}

} // extern "C"
