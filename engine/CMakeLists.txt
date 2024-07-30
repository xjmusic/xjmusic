cmake_minimum_required(VERSION 3.24)

# Project name and a few useful settings. Other commands can pick up the results
project(
        xjmusic
        VERSION 1.9.3
        DESCRIPTION "The XJ music engine"
        LANGUAGES CXX)

# C++ 17
set(CMAKE_CXX_STANDARD 17)

# Only do these if this is the main project, and not if it is included through add_subdirectory
if (CMAKE_PROJECT_NAME STREQUAL PROJECT_NAME)

    # Optionally set things like CMAKE_CXX_STANDARD, CMAKE_POSITION_INDEPENDENT_CODE here

    # Let's ensure -std=c++xx instead of -std=g++xx
    set(CMAKE_CXX_EXTENSIONS OFF)

    # Let's nicely support folders in IDEs
    set_property(GLOBAL PROPERTY USE_FOLDERS ON)

    # Testing only available if this is the main app
    # Note this needs to be done in the main CMakeLists
    # since it calls enable_testing, which must be in the
    # main CMakeLists.
    include(CTest)

    # Docs only available if this is the main app
    find_package(Doxygen)
    if (Doxygen_FOUND)
        add_subdirectory(doc)
    else ()
        message(STATUS "Doxygen not found, not building docs")
    endif ()
endif ()

# Function to add a vendor external project
function(add_vendor_external_project PARENT NAME ARCHIVE_FILE HASH_MD5 INSTALLED_BUILD_BYPRODUCTS COPY_TO_RUNTIME_LOCATION)
    set(INSTALLED_DIR ${CMAKE_BINARY_DIR}/vendor/installed)

    set(BUILD_BYPRODUCTS "")
    foreach (BYPRODUCT ${INSTALLED_BUILD_BYPRODUCTS})
        list(APPEND BUILD_BYPRODUCTS "${INSTALLED_DIR}/${BYPRODUCT}")
    endforeach ()

    ExternalProject_Add(${NAME}
            PREFIX "${CMAKE_BINARY_DIR}/vendor"
            URL "${CMAKE_SOURCE_DIR}/vendor/${ARCHIVE_FILE}"
            URL_HASH MD5=${HASH_MD5}
            CMAKE_ARGS -DCMAKE_INSTALL_PREFIX=${INSTALLED_DIR}
            BUILD_BYPRODUCTS ${BUILD_BYPRODUCTS}
    )

    include_directories(${INSTALLED_DIR}/include)
    set_target_properties(${NAME} PROPERTIES EXCLUDE_FROM_ALL TRUE)
    add_dependencies(${PARENT} ${NAME})

    foreach (BYPRODUCT ${BUILD_BYPRODUCTS})
        target_link_libraries(${PARENT} PRIVATE ${BYPRODUCT})
    endforeach ()

    foreach (BYPRODUCT ${COPY_TO_RUNTIME_LOCATION})
        set(RUNTIME_LOCATION $<TARGET_FILE_DIR:${PARENT}>)
        add_custom_command(TARGET ${PARENT} POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E copy
                ${INSTALLED_DIR}/${BYPRODUCT}
                ${RUNTIME_LOCATION})
        message(STATUS "Copying ${BYPRODUCT} to runtime location ${RUNTIME_LOCATION}")
    endforeach ()
endfunction()

# FetchContent added in CMake 3.11, downloads during the configure step
include(FetchContent)

# The compiled library code is here`
add_subdirectory(src)

# The executable code is here
add_subdirectory(example)

# Testing only available if this is the main app
# Emergency override MODERN_CMAKE_BUILD_TESTING provided as well
if ((CMAKE_PROJECT_NAME STREQUAL PROJECT_NAME OR MODERN_CMAKE_BUILD_TESTING)
        AND BUILD_TESTING)
    add_subdirectory(test)
endif ()

