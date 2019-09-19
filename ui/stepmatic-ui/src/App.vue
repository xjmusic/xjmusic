<template>
  <div id="app">
    <SequenceSelector></SequenceSelector>
  </div>
</template>

<script>
  import {mapState} from 'vuex'
  import SequenceSelector from './components/SequenceSelector.vue'

  export default {
    name: 'app',
    computed: {
      ...mapState([
        'program'
      ])
    },
    components: {
      SequenceSelector
    }
  }

</script>

<style lang="scss">
  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: #000;
    margin: 0 0;
    display: flex;
    flex-direction: column;
  }
</style>
