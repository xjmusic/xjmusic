/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import {configureStore} from "@reduxjs/toolkit"
// app
import reducers from './reducers';
import middleware from './middleware';

/**

 +----------------+
 | HERE BE REDUX! |
 +----------------+

 Redux with redux-api-middleware and a reducer to redux-orm
 - [redux](redux.js.org)-- see [the tutorial](https://redux.js.org/basics/actions)
 - [react-router](https://www.npmjs.com/package/react-router) and [react-router-dom](https://www.npmjs.com/package/react-router-dom) within new `/mk3` path
 - [redux-toolkit](https://redux-toolkit.js.org/)— includes `redux-thunk` and `immer`
 - [redux-api-middleware](https://github.com/agraboso/redux-api-middleware) loads entity data from the backend api
 - [redux-orm](https://github.com/redux-orm/redux-orm) and when successful API request is returned, update redux-orm store
 - [react-redux-toastr](https://www.npmjs.com/package/react-redux-toastr) and errors returned by API during save are displayed in flash messages

 */
export default configureStore({
  reducer: reducers,
  middleware: middleware,
});
