set(DOXYGEN_EXTRACT_ALL YES)
set(DOXYGEN_BUILTIN_STL_SUPPORT YES)

set(DOXYGEN_PROJECT_NAME "XJ music engine")
set(DOXYGEN_PROJECT_NUMBER "1.7.41")
set(DOXYGEN_PROJECT_BRIEF "In-game runtime engine for XJ music.")
set(DOXYGEN_PROJECT_ICON "${CMAKE_SOURCE_DIR}/doc/xj-music-logo.png")
set(DOXYGEN_CREATE_SUBDIRS "YES")
set(DOXYGEN_USE_MDFILE_AS_MAINPAGE "${CMAKE_SOURCE_DIR}/doc/index.md")
set(DOXYGEN_EXCLUDE_SYMBOLS "detail,json_literals,literals::json_literals,literals,nlohmann,std,std::*,adl_serializer,byte_container_with_subtype,basic_json,json_pointer,json_sax,std::vector,ordered_map")
set(DOXYGEN_GENERATE_HTML "YES")
set(DOXYGEN_SITEMAP_URL "https://engine-docs.xjmusic.com/sitemap.xml")

doxygen_add_docs(doc
        "${CMAKE_SOURCE_DIR}/LICENSE.md"
        "${CMAKE_SOURCE_DIR}/include"
        "${CMAKE_SOURCE_DIR}/src"
        "${CMAKE_SOURCE_DIR}/doc/index.md"
        "${CMAKE_SOURCE_DIR}/doc/xj-music-logo.png"
)
