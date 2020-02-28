/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// vendor
import React from 'react'
import ReactDOM from 'react-dom'
// app
import App from './App'

// root element
const root = document.getElementById('root');

/**
 <Provider store={store}> is the implementation of react-redux
 */
function main() {
  ReactDOM.render(<App/>, root);
}

// run it
main();
