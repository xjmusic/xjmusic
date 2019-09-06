<template>
  <div id="app">
    <!--    <img alt="Vue logo" src="./assets/logo.png">-->

  </div>
</template>

<script>

  import HelloWorld from './components/HelloWorld.vue'

  export default {
    name: 'app',
    components: {
      HelloWorld
    },
    mounted() {
      this.params = getParams();
      let id = this.params.id;
      fetch(`/api/1/programs/${id}`).then((data)=>{
        console.log(data);
      });
    }
  }

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

</script>

<style lang="scss">
  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    margin-top: 60px;
  }
</style>
