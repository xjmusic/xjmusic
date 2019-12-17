// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import 'react-redux-toastr/src/styles/index.scss'
import React from 'react'
import ReduxToastr, {reducer as toastrReducer} from "react-redux-toastr";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom"
import {createBrowserHistory} from 'history'
import {useParams} from "react-router"
import {Provider} from "react-redux";
// app
import './App.scss'
import ProgramEditor from "./components/program/ProgramEditor"
import store from "./store";
import {configureStore} from "@reduxjs/toolkit";


//  run router with base URL
export const history = createBrowserHistory({
  basename: process.env.PUBLIC_URL
});

const toastrStore = configureStore({
  reducer:
    {
      toastr: toastrReducer,
    }
});

/**
 App with react-router-dom
 */
export default function App() {
  return (
    <div id="app">
      <Provider store={toastrStore}>
        <ReduxToastr
          position="bottom-right"
          transitionIn="fadeIn"
          transitionOut="fadeOut"
          closeOnToastrClick/>
      </Provider>
      <Provider store={store}>
        <Router basename="/mk3">
          <main role="main">
            <Switch>
              <Route exact path="/"><Home/></Route>
              <Route path="/programs/:id"><Program/></Route>
            </Switch>
          </main>
        </Router>
      </Provider>
    </div>
  );
}

/**
 Route: /
 */
function Home() {
  return (
    <div>
      <h2>Home</h2>
    </div>
  );
}

/**
 Route: /programs/:id
 */
function Program() {
  // We can use the `useParams` hook here to access
  // the dynamic pieces of the URL.
  let {id} = useParams();

  return (
    <ProgramEditor programId={id}/>
  );
}
