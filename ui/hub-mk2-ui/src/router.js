// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import ProgramEditor from "./components/ProgramEditor";

export default {
  mode: 'history',
  base: '/mk2/',
  routes: [
    {path: '/programs/:program_id', component: ProgramEditor},
  ]
};
