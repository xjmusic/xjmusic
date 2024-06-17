set(DOXYGEN_EXTRACT_ALL YES)
set(DOXYGEN_BUILTIN_STL_SUPPORT YES)

set(DOXYGEN_PROJECT_NAME "XJ music engine")
set(DOXYGEN_PROJECT_NUMBER "0.1")
set(DOXYGEN_PROJECT_BRIEF "This is the playback engine for XJ music.")
set(DOXYGEN_PROJECT_ICON "${CMAKE_SOURCE_DIR}/doc/xj-music-logo.png")
set(DOXYGEN_CREATE_SUBDIRS "YES")

doxygen_add_docs(doc
        "${CMAKE_SOURCE_DIR}/include"
        "${CMAKE_SOURCE_DIR}/src"
        "${CMAKE_SOURCE_DIR}/README.md"
        "${CMAKE_SOURCE_DIR}/LICENSE.md"
        "${CMAKE_SOURCE_DIR}/doc/xj-music-logo.png"
)