// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import actions from './actions';
import getters from './getters';
import mutations from './mutations';
import VueRestAPI from "vuex-rest-api"

// Programs
const api = new VueRestAPI({
  baseURL: "https://jsonplaceholder.typicode.com",
  state: {
    posts: []
  }
})
// Step 3
  .get({
    action: "getPost",
    property: "post",
    path: ({ id }) => `/posts/${id}`
  })
  .get({
    action: "listPosts",
    property: "posts",
    path: "/posts"
  })
  .post({
    action: "updatePost",
    property: "post",
    path: ({ id }) => `/posts/${id}`
  })
  // Step 4
  .getStore();

let timelineGrids = [
  {
    name: "1/4",
    value: 0.25
  },
  {
    name: "1/8",
    value: 0.125
  },
  {
    name: "1/16",
    value: 0.625
  },
  {
    name: "1/32",
    value: 0.03125
  },
];

export default {
  state: {
    program: {},
    dirty: false,
    saving: false,

    // timeline grid options
    timelineGrids: timelineGrids,

    // UI current state
    activeSequence: null,
    activePattern: {},
    activeTimelineGrid: timelineGrids[0],

    // geometry
    trackHeight: 200,
    primaryWidth: 200,
    secondaryWidth: 120,
    beatWidth: 100,
  },
  actions,
  getters,
  mutations,
}
