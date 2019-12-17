<template>
  <div class="track-editor" v-bind:style="{flexBasis:`${$store.state.trackHeight}px`}">
    <div class="name" v-bind:style="{flexBasis:`${$store.state.secondaryWidth}px`}">
      <h3>{{track.name}}</h3>
    </div>
    <div class="events" v-if="pattern">
      <div class="event-container">
        <div class="event"
             v-for="event in $store.getters.getEventsForPatternTrack(pattern, track)"
             v-bind:key="event.id"
             v-bind:style="{
              left:`${$store.state.beatWidth * event.position}px`,
              width:`${$store.state.beatWidth * event.duration}px`,
              height:`${$store.state.trackHeight * event.velocity}px`,
              top:`${$store.state.trackHeight / 2 - $store.state.trackHeight * event.velocity / 2}px`
           }">
          &nbsp;
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'TrackEditor',
    props: {
      'track': Object,
      'pattern': Object,
    },
    methods: {}
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
  @import "../palette";

  .track-editor {
    display: flex;
    flex-direction: row;
    background: $track-editor-background;

    .name {
      border-right: $component-border;
    }

    &:not(:last-child) {
      border-bottom: $component-border;
    }

    h3 {
      margin-top: 5px;
      margin-right: 5px;
      color: $track-name-color;
      font-size: 1em;
      position: relative;
      top: 42%;
      text-align: right;
    }

    .events {
      flex-grow: 1;
      background-color: $track-events-background;
      background-image: url(./../assets/track-events-background.png);
      background-repeat: repeat-x;
      background-position: left center;

      .event-container {
        position: relative;
        display: block;

        .event {
          position: absolute;
          display: block;
          cursor: move;
          border: $event-border;
          background-color: $event-background-color;
        }
      }
    }
  }

</style>
