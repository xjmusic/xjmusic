set(DOXYGEN_EXTRACT_ALL YES)
set(DOXYGEN_BUILTIN_STL_SUPPORT YES)

set(DOXYGEN_PROJECT_NAME "XJ music engine")
set(DOXYGEN_PROJECT_NUMBER "1.7.41")
set(DOXYGEN_PROJECT_BRIEF "In-game runtime engine for XJ music.")
set(DOXYGEN_PROJECT_ICON "${CMAKE_SOURCE_DIR}/doc/xj-music-logo.png")
set(DOXYGEN_CREATE_SUBDIRS "YES")
set(DOXYGEN_USE_MDFILE_AS_MAINPAGE "${CMAKE_SOURCE_DIR}/doc/README.md")
set(DOXYGEN_EXCLUDE_SYMBOLS "detail::*,*::detail::*,std::*,*::std::*,adl_serializer::*,*::adl_serializer::*,basic_json::*,*::basic_json::*,byte_container_with_subtype::*,*::byte_container_with_subtype::*,json_pointer::*,*::json_pointer::*,json_sax::*,*::json_sax::*,ordered_map::*,*::ordered_map::*,")

doxygen_add_docs(doc
        "${CMAKE_SOURCE_DIR}/LICENSE.md"
        "${CMAKE_SOURCE_DIR}/include"
        "${CMAKE_SOURCE_DIR}/src"
        "${CMAKE_SOURCE_DIR}/doc/README.md"
        "${CMAKE_SOURCE_DIR}/doc/xj-music-logo.png"
)
