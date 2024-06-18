set(DOXYGEN_EXTRACT_ALL YES)
set(DOXYGEN_BUILTIN_STL_SUPPORT YES)

set(DOXYGEN_PROJECT_NAME "XJ music engine")
set(DOXYGEN_PROJECT_NUMBER "1.7.40")
set(DOXYGEN_PROJECT_BRIEF "In-game runtime engine for XJ music.")
set(DOXYGEN_PROJECT_ICON "${CMAKE_SOURCE_DIR}/doc/xj-music-logo.png")
set(DOXYGEN_CREATE_SUBDIRS "YES")
set(DOXYGEN_USE_MDFILE_AS_MAINPAGE "${CMAKE_SOURCE_DIR}/README.md")

doxygen_add_docs(doc
        "${CMAKE_SOURCE_DIR}/include"
        "${CMAKE_SOURCE_DIR}/src"
        "${CMAKE_SOURCE_DIR}/README.md"
        "${CMAKE_SOURCE_DIR}/LICENSE.md"
        "${CMAKE_SOURCE_DIR}/doc/xj-music-logo.png"
)
