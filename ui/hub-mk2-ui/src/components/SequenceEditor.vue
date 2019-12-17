<template>
  <!-- $store.state.activeSequence -->
  <div class="sequence-editor">
    <VoiceEditor v-for="voice in $store.state.program.voices"
                 v-bind:voice="voice">
    </VoiceEditor>
  </div>
</template>

<script>
  import VoiceEditor from "./VoiceEditor";

  export default {
    name: 'SequenceEditor',
    components: {
      VoiceEditor
    },
    props: {},
    methods: {}
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
  @import "../palette";

  .sequence-editor {
    display: flex;
    flex-direction: column;
    flex-grow: 100;;
    background-color: $panel-background-color;
  }
</style>
