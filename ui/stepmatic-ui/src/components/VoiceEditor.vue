<template>
  <div class="voice-editor">

    <!-- Voice editing controls -->
    <div class="controls" v-bind:style="{width:`${$store.state.primaryWidth}px`}">
      <h2>{{voice.name}}</h2>
      <div class="pattern-selector" v-bind:style="{flexBasis:`${$store.state.primaryWidth}px`}">
        <v-select
          :options="$store.getters.getPatternsForVoice(voice)"
          :value="pattern"
          :clearable="false"
          :get-option-label="getPatternLabel"
          @input="selectPattern"
        />
        <a class="edit-button" v-if="pattern" v-on:click="editPattern(pattern)">Edit</a>
      </div>
    </div>

    <!-- Tracks in currently selected voice -->
    <div class="tracks">
      <TrackEditor v-for="track in $store.getters.getTracksForVoice(voice)"
                   v-bind:track="track"
                   v-bind:pattern="pattern"
                   v-bind:key="track.id">
      </TrackEditor>
    </div>

  </div>
</template>

<script>
  import TrackEditor from "./TrackEditor";

  export default {
    name: 'VoiceEditor',
    data() {
      return {
        'pattern': null
      };
    },
    props: {
      'voice': Object,
    },
    components: {
      TrackEditor
    },
    methods: {
      selectPattern(pattern) {
        this.pattern = pattern;
      },
      getPatternLabel(pattern) {
        return `${pattern.name} (${pattern.type})`;
      },
      editPattern(pattern) {
        this.$modal.show('edit-pattern', {
          pattern: pattern,
          onDelete: () => {
            this.pattern = null;
          }
        });
      },
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
  @import "../palette";

  .voice-editor {
    display: flex;
    flex-direction: row;
    border-bottom: $component-border;

    .controls {
      border-right: $component-border;

      h2 {
        margin-top: 5px;
        margin-left: 5px;
        color: #808080;
        font-size: 1em;
        /*
        TODO voice editor voice name labels are rotated 90 degrees and centered in track
        transform: rotate(-90deg);
         */
      }

      .edit-button {
        @include edit-button($action-color);
      }
    }

    .tracks {
      flex-grow: 100;
      display: flex;
      flex-direction: column;
    }
  }
</style>
