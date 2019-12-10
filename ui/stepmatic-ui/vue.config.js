// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
module.exports = {
  publicPath: '/stepmatic/',
  devServer: {
    host: 'localhost',

    // [#168819062] Stepmatic Vue.js environment proxies /api/1 to docker-compose hub setup
    proxy: {
      "/api/1/*": {
        target: "http://localhost/",
        secure: false
      }
    }
  }
};
