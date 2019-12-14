<template>
  <modal name="edit-pattern" class="vue-dialog" :adaptive="true" :clickToClose="false" @before-open="beforeOpen">
    <div class="dialog-content">
      <div v-if="pattern" class="form">

        <!-- Edit type -->
        <div class="row">
          <label class="label" for="pattern-type">Type</label>
          <v-select id="pattern-type" v-model="pattern.type" :options="patternTypes" :clearable="false"
                    class="field"></v-select>
        </div>

        <!-- Edit name -->
        <div class="row">
          <label class="label" for="pattern-name">Name</label>
          <input id="pattern-name" type="text" v-model="pattern.name" class="field"/>
        </div>

        <!-- Edit total -->
        <div class="row">
          <label class="label" for="pattern-total">Beats</label>
          <input id="pattern-total" type="number" v-model="pattern.total" class="field"/>
        </div>

        <!-- or -->
        <div class="row">
          <label class="label" for="pattern-total">&nbsp;</label>
        </div>

        <!-- Delete -->
        <div class="row">
          <label class="label" for="pattern-total">or</label>
          <div class="delete-button" @click="destroy">Delete Pattern</div>
        </div>

      </div>
    </div>
    <div class="vue-dialog-buttons">
      <button v-if="patternModified" type="button" class="vue-dialog-button success" @click="save">SAVE</button>
      <button type="button" class="vue-dialog-button" @click="cancel">CANCEL</button>
    </div>
  </modal>
</template>

<script>
  export default {
    name: 'PatternEditModal',
    data() {
      return {
        'pattern': null,
        'onDelete': null,
        'patternTypes': ['Intro', 'Outro', 'Loop'],
        'originalPattern': Object,
        'patternModified': false,
      };
    },
    props: {},
    components: {},
    methods: {
      beforeOpen(event) {
        this.pattern = this._.cloneDeep(event.params.pattern);
        this.originalPattern = this._.cloneDeep(event.params.pattern);
        if (event.params.hasOwnProperty("onDelete")) {
          this.onDelete = event.params.onDelete;
        }
      },
      save() {
        this.$store.commit('updateEntity', {
          type: 'patterns',
          id: this.pattern.id,
          attrs: {
            name: this.pattern.name,
            type: this.pattern.type,
            total: this.pattern.total,
          }
        });
        this.$modal.hide('edit-pattern');
      },
      destroy() {
        if (confirm("Destroy pattern and its events?")) {
          this.$store.commit('destroyPattern', this.pattern.id);
          if (this.onDelete) this.onDelete();
          this.$modal.hide('edit-pattern');
        }
      },
      cancel() {
        this.$modal.hide('edit-pattern');
      }
    },
    watch: {
      pattern: {
        handler(updated) {
          this.patternModified = !this._.isEqual(updated, this.originalPattern);
        },
        deep: true
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
  @import "../../palette";

  .vue-dialog {
    .delete-button {
      @include delete-button($action-color);
    }
  }
</style>

