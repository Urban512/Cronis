cmake_minimum_required(VERSION 3.20)
project(cronis_smtc LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

if(NOT DEFINED JAVA_HOME)
    message(FATAL_ERROR "JAVA_HOME must be defined for JNI headers")
endif()

find_package(JNI REQUIRED)

add_library(cronis_smtc SHARED
        cronis_smtc.cpp
)

target_include_directories(cronis_smtc PRIVATE
        ${JNI_INCLUDE_DIRS}
        ${JNI_INCLUDE_DIRS}/win32
)

target_compile_definitions(cronis_smtc PRIVATE
        UNICODE
        _UNICODE
        NOMINMAX
        WIN32_LEAN_AND_MEAN
)

target_compile_options(cronis_smtc PRIVATE
        /EHsc
        /Zc:twoPhase-
        /permissive-
        /W4
)

target_link_libraries(cronis_smtc PRIVATE
        windowsapp
)

set_target_properties(cronis_smtc PROPERTIES
        OUTPUT_NAME "cronis_smtc"
)
