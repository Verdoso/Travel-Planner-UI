<template>
    <section>
      <H1>Travel planner demo</H1>

      <!-- Search by IP -->
      <b-field label="Base IP" label-position="on-border">
          <b-input placeholder="IP..." type="search" v-model="publicIp"></b-input>
          <p class="control">
              <b-button class="button is-primary">Find closest airports</b-button>
          </p>
      </b-field>
    </section>
</template>

<script>
    import axios from "axios";

    export default {
        props: []
        ,data() {
            return {
                isLoading: false,
                publicIp: null
            }
        }
        , mounted() {
            axios
              .get('https://api64.ipify.org?format=json')
              .then(response => {
                this.publicIp = response.data.ip;
              })
              .catch(e => {
                console.error(e);
              });
        }
        , methods: {
        }
        , computed: {
        }
    }
</script>