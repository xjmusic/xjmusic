import VueResource from 'vue-resource';
import App from './App';
import Vue from 'vue/dist/vue.js';
import Vuex from 'vuex';
import actions from './actions';
import getters from './getters';
import mutations from './mutations';
import api from './api';

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


// Vue.config.productionTip = false;

Vue.use(Vuex);
Vue.use(VueResource);

Vue.http.options.root = '/api/1/';

let params = getParams();
let id = params.id;
Vue.http.get(`programs/${id}`)
  .then(response => response.json())
  .then(data => api.deserialize(data))
  .then(program => {
    /* eslint-disable no-new */
    new Vue({
      el: '#app',
      store: new Vuex.Store({
        state: {
          program: program,
        },
        actions,
        getters,
        mutations,
      }),
      template: '<App/>',
      components: {App}
    });
  });

