// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import store from './store';
import router from './router';
import Vue from 'vue/dist/vue.js';
import App from './App';
import VueResource from 'vue-resource';
import Vuex from 'vuex';
import vSelect from 'vue-select';
import VueJSModal from "vue-js-modal";
import VueLodash from "vue-lodash";
import VueFlashMessage from 'vue-flash-message';
import VueRouter from "vue-router";

/**
 * XSS attack protection
 * @returns {Object} key-values of options found after hash in URL
 */
function getParams() {
  let url = document.URL;
  let hashPos = url.indexOf('#');
  if (hashPos < 0) return {};
  let hashString = url.substr(hashPos + 1);
  let options = {};
  hashString.replace(/([^=&]+)=([^&]*)/gi, function (m, key, value) {
    if (value > 0 || value < 0) {
      options[key] = Number(value);
    } else {
      options[key] = value;
    }
  });
  return options;
}

Vue.use(Vuex);
Vue.use(VueJSModal);
Vue.use(VueLodash);
Vue.use(VueFlashMessage);
Vue.component('v-select', vSelect);

// Vue resource config
Vue.use(VueResource);
Vue.http.options.root = '/api/1/';
Vue.http.interceptors.push((request, next) => {
  request.headers.set('Content-Type', 'application/vnd.api+json');
  next()
});

// Router
Vue.use(VueRouter);

/* eslint-disable no-new */
window.mainVue = new Vue({
  el: '#app',
  router: new VueRouter(router),
  store: new Vuex.Store(store),
  components: {App},
  template: '<App/>'
}).$mount('#app');

/*

TODO remove legacy params based bootstrap

let params = getParams();
let id = params.id;
if (id)
  Vue.http.get(`programs/${id}`)
    .then(response => response.json())
    .then(data => api.deserializer.deserialize(data))
    .then(program => {
      /!* eslint-disable no-new *!/
      window.mainVue = new Vue({
        el: '#app',
        router: new VueRouter({routes}),
        store: new Vuex.Store(store(program)),
        components: {App},
        template: '<App/>'
      }).$mount('#app');
    })
    .catch(error => {
      alert(`${error.statusText}! (Code ${error.status})`);
    });
else
  alert("Can't start Stepmatic with Program to load!");
*/
