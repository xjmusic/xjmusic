<template>
  <div id="app">
    <flash-message class="flash_messages_container"></flash-message>
    <ProgramEditor></ProgramEditor>
    <PatternEditModal></PatternEditModal>
  </div>
</template>

<script>
  import {mapState} from 'vuex'
  import PatternEditModal from "./components/modal/PatternEditModal";
  import ProgramEditor from "./components/ProgramEditor";

  export default {
    name: 'app',
    computed: {
      ...mapState([
        'program'
      ])
    },
    components: {
      ProgramEditor,
      PatternEditModal,
    }
  }

</script>

<style lang="scss">
  @import "vue-select/src/scss/vue-select.scss";
  @import "palette";

  html, body {
    margin: 0 0;
    padding: 0 0;
    height: 100%;
    background-color: $app-background-color;
  }

  #app {
    /*font-family: 'Avenir', Helvetica, Arial, sans-serif;*/
    font-family: 'Roboto Mono', 'Courier New', monospace;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: $app-font-color;
    margin: 0 0;
    display: flex;
    flex-direction: column;
    flex: 1;
    height: 100%;
  }
</style>

<!-- Globally override styles for modals, inputs -->
<style lang="scss">
  @import "palette";

  #app {
    .v--modal {
      background-color: $modal-background-color;
    }

    .vue-dialog .v--modal {
      display: flex;
      flex-direction: column;

      .vue-dialog-buttons {
        border-top-color: darken($modal-background-color, 20%);
      }

      .vue-dialog-button {
        @include dialog-button-color($modal-background-color);
        flex-grow: 100;

        &.success {
          @include dialog-button-color($success-background-color);
        }
      }
    }

    .form {
      display: flex;
      flex-direction: column;

      .row {
        display: flex;
        flex-direction: row;
        align-items: center;
        margin-bottom: 10px;

        .label {
          text-transform: uppercase;
          color: $label-color;
          font-weight: bold;
          font-size: 0.8em;
          flex-basis: 15%;
          text-align: right;
          padding-right: 5px;
        }

        .field {
          flex-grow: 100;
        }

        input.field {
          border-color: lighten($input-background-color, 10%) lighten($input-background-color, 20%) lighten($input-background-color, 30%) lighten($input-background-color, 20%);
          background-color: $input-background-color;
          color: $input-color;
          padding: 5px;
        }

      }
    }

    .v-select {
      background-color: $input-background-color;

      .vs__selected {
        color: $input-color;
      }

      .vs__dropdown-menu {
        background-color: $input-background-color;
        color: $input-color;
      }

      .vs__dropdown-option {
        color: $input-color;
      }

      .vs__dropdown-toggle {
        padding: 0 0 0 0;
      }
    }
  }

  .flash_messages_container {
    position: absolute;
    top: 10px;
    left: 33%;
    width: 33%;
    text-wrap: normal;
  }

  .flash__message {
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    padding: 15px 30px 15px 15px;
    margin-bottom: 10px;
    border: 1px solid transparent;
    border-radius: 4px;
    background-color: #cccccc;
    position: relative;
  }

  .flash__message.success {
    color: #3c763d;
    background-color: #dff0d8;
    border-color: #d6e9c6;
  }

  .flash__message.warning {
    color: #8a6d3b;
    background-color: #fcf8e3;
    border-color: #faebcc;
  }

  .flash__message.error {
    color: #a94442;
    background-color: #f2dede;
    border-color: #ebccd1;
  }

  .flash__message.info {
    color: #37516A;
    background-color: #F2F5FE;
    border-color: rgb(223, 227, 240);
  }

  .flash__close-button {
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    position: absolute;
    top: 0px;
    right: 0px;
    color: inherit;
    -webkit-appearance: none;
    -moz-appearance: none;
    appearance: none;
    background-color: transparent;
    background: none;
    border: none;
    cursor: pointer;
    font-size: 18px;
    line-height: 10px;
    margin: 0;
    padding: 8px;
  }

  .flash__close-button:active, .flash__close-button:focus {
    outline: none;
  }

</style>
