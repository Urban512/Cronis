param(
    [switch]$Probe
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Write-Stage {
    param(
        [string]$Name,
        [string]$Detail = ''
    )

    if ([string]::IsNullOrWhiteSpace($Detail)) {
        Write-Output "stage=$Name"
        return
    }

    Write-Output "stage=$Name"
    Write-Output "detail=$Detail"
}

function Await-WinRt {
    param(
        $WinRtTask,
        [Type]$ResultType
    )

    Add-Type -AssemblyName System.Runtime.WindowsRuntime
    $asTaskGeneric = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
        $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'
    })[0]
    $asTask = $asTaskGeneric.MakeGenericMethod($ResultType)
    $netTask = $asTask.Invoke($null, @($WinRtTask))
    $netTask.Wait(-1) | Out-Null
    return $netTask.Result
}

try {
    [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media, ContentType = WindowsRuntime] | Out-Null

    if ($Probe) {
        Write-Stage -Name 'winrt_initialized'
        exit 0
    }

    $manager = Await-WinRt ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])
    Write-Stage -Name 'session_manager_acquired'

    $session = $manager.GetCurrentSession()
    if ($null -eq $session) {
        Write-Stage -Name 'no_current_session'
        exit 0
    }

    $appId = $session.SourceAppUserModelId
    Write-Output "app_id=$appId"

    if ($appId -notmatch '(?i)spotify') {
        Write-Stage -Name 'not_spotify' -Detail $appId
        exit 0
    }

    $properties = Await-WinRt ($session.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])
    $playbackInfo = $session.GetPlaybackInfo()
    $timeline = $session.GetTimelineProperties()

    $title = [string]$properties.Title
    if ([string]::IsNullOrWhiteSpace($title)) {
        Write-Stage -Name 'metadata_empty' -Detail $appId
        exit 0
    }

    Write-Stage -Name 'spotify_session'
    Write-Output "app_name=Spotify"
    Write-Output "session_id=$appId"
    Write-Output "title=$title"
    Write-Output "artist=$([string]$properties.Artist)"
    Write-Output "album_title=$([string]$properties.AlbumTitle)"
    Write-Output "album_artist=$([string]$properties.AlbumArtist)"
    Write-Output "playback_status=$([int]$playbackInfo.PlaybackStatus)"
    Write-Output "position_ms=$([int64]($timeline.Position.Ticks / 10000))"
    Write-Output "duration_ms=$([int64](($timeline.EndTime.Ticks - $timeline.StartTime.Ticks) / 10000))"
    exit 0
} catch {
    Write-Stage -Name 'error' -Detail $_.Exception.Message
    exit 1
}
