<template>
  <div class="program-editor">

    <div class="header">
      <div class="main">
        <div class="name">{{$store.state.program.name}}</div>
      </div>

      <div class="version-control">
        <div v-if="!$store.state.saving && $store.state.dirty" class="button dirty" @click="save">
          <div class="content">Save</div>
        </div>
        <div v-if="!$store.state.saving && !$store.state.dirty" class="button clean">
          <div class="content">&#10003;</div>
        </div><!-- TODO no action here; for debugging only! -->
        <div class="button saving" disabled v-if="$store.state.saving">
          <div class="content saving-animation">
            <div></div>
            <div></div>
            <div></div>
            <div></div>
          </div>
        </div>
      </div>
    </div>

    <div class="navigation">
      <div class="sequence-selector" v-bind:style="{flexBasis:`${$store.state.primaryWidth}px`}">
        <v-select
          :options="$store.state.program.sequences"
          :value="$store.state.activeSequence"
          :clearable="false"
          label="name"
          @input="selectSequence"
        />
      </div>
      <div class="timeline-selector" v-bind:style="{flexBasis:`${$store.state.secondaryWidth}px`}">
        <v-select
          :clearable="false"
          :options="$store.state.timelineGrids"
          :value="$store.state.activeTimelineGrid"
          label="name"
          @input="selectTimelineGrid"
        />
      </div>
      <div class="timeline-navigator">
        <!-- TODO TimelineNavigator displays timeline based on shared state of timeline size and divisions -->
        <!-- TODO TimelineNavigator selects divisions (1, 1/2, 1/4, 1/8, 1/16) -->
        <!-- TODO TimelineSelector sets the displayed grid subdivisions -->
      </div>
    </div>

    <SequenceEditor></SequenceEditor>
    <PatternEditModal></PatternEditModal><!-- TODO Consider using dynamic modals -->
  </div>
</template>

<script>
  import SequenceEditor from "./SequenceEditor";
  import PatternEditModal from "./modal/PatternEditModal";

  export default {
    name: 'ProgramEditor',
    props: {},
    methods: {
      save() {
        this.$store.dispatch('saveProgram')
          .then(() => {
            // TODO on successful save
          })
          .catch(() => {
            // TODO on failure to save
          })
      },
      selectSequence(sequence) {
        this.$store.commit('setActiveSequence', sequence);
      },
      selectTimelineGrid(grid) {
        this.$store.commit('setActiveTimelineGrid', grid);
      },
    },
    components: {
      SequenceEditor,
      PatternEditModal,
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
  @import "../palette";

  .program-editor {
    display: flex;
    flex-direction: column;
    background-color: $header-background-color; // temp

    .header {
      display: flex;
      flex-direction: row;

      .main {
        flex-grow: 1;
        .name {
          font-size: 24px;
          font-weight: bold;
        }
      }

      .version-control {
        .button {
          &.dirty {
            @include action-button-style($warning-color);
          }

          &.clean {
            @include inaction-button-style($success-background-color);
          }

          &.saving {
            @include inaction-button-style($action-background-color);
            @include saving-animation($action-color);
          }
        }
      }
    }

    .navigation {
      display: flex;
      flex-direction: row;
      background-color: $header-background-color; // temp

      .sequence-selector {

      }

      .timeline-selector {

      }

      .timeline-navigator {

      }
    }
  }

</style>
