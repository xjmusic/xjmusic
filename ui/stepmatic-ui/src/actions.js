import Vue from 'vue/dist/vue.js';
import api from './api';

export default {

  /**
   * Program is saved, serializing program and posting changes to API
   */
  saveProgram({state, commit}) {
    commit("willSave");
    Vue.http.patch(`programs/${state.program.id}`, JSON.stringify(api.serializer.serialize(state.program)))
      .then(response => response.json())
      .then(data => api.deserializer.deserialize(data))
      .then(program => {
        commit("didSave", program);
      })
      .catch(error => {
        commit("failedToSave", error);
      });
  },

};
