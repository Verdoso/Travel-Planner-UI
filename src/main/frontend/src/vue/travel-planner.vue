<template>
    <section>
      <div class="card">
        <div class="card-content">
          <div class="content"  style="min-height: 35em;">
            <H1>Travel planner demo</H1>
            <div class="columns">
              <div class="column is-one-quarter">
                <!-- Search by IP -->
                <b-field label="Base IP" label-position="on-border">
                    <b-input placeholder="IP..." type="search" v-model="publicIp"></b-input>
                    <p class="control">
                        <b-button
                          class="button
                          is-primary"
                          :loading="isFinding"
                          @click="findClosestAirports"
                          >Find closest airports</b-button>
                    </p>
                </b-field>
              </div>
              <div class="column is-one-quarter">
                <b-field v-if="this.closestAirportsInfo" label="From" horizontal>
                    <b-select placeholder="Airport" icon="airplane-takeoff">
                        <option v-for="distance in this.closestAirportsInfo.distances"
                          value="1"
                        >
                          ({{ distance.airport.iataFaaCode }}) - <strong>{{ distance.airport.name }}</strong> (at {{ distance.distance.kilometers }}Km)
                        </option>
                    </b-select>
                </b-field>
              </div>
              <div class="column is-one-quarter">
                <div class="field has-addons">
                  <b-field label="From" horizontal>
                    <b-datepicker
                        v-model="from"
                        :first-day-of-week="1"
                        :min-date="new Date()"
                        placeholder="Departure date"
                        icon="calendar-today"
                        trap-focus
                        >
                    </b-datepicker>
                    <p class="control">
                      <b-button
                          @click="() => { from = null; }"
                          icon-left="delete"
                          type="is-info" />
                    </p>
                  </b-field>
                </div>
              </div>
              <div class="column is-one-quarter">
                <div class="field has-addons">
                  <b-field label="To" horizontal v-if="from">
                    <b-datepicker
                        v-model="to"
                        :first-day-of-week="1"
                        :min-date="from"
                        placeholder="Arrival date"
                        icon="calendar-today"
                        trap-focus
                        >
                    </b-datepicker>
                    <p class="control">
                      <b-button
                          @click="() => { to = null; }"
                          icon-left="delete"
                          type="is-info" />
                    </p>
                  </b-field>
                </div>
              </div>
            </div>
            <div class="columns">
              <div class="column is-one-quarter">
                <b-rate custom-text="Adults" icon="human-greeting" v-model="adults"></b-rate>
                <b-rate custom-text="Children" icon="human-child" v-model="children"></b-rate>
              </div>
            </div>
          </div> <!-- End content -->
        </div>
      </div>
    </section>
</template>

<script>
    import axios from "axios";

    export default {
        props: []
        ,data() {
            return {
                isFinding: false,
                publicIp: null,
                closestAirportsInfo : null,
                from: null,
                to: null,
                adults: 2,
                children: 0
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
          findClosestAirports () {
            if(!this.isFinding && this.publicIp) {
              this.isFinding = true;
              console.error('Finding closest airports!');
              try {
                axios
                  .get('/travel-planner/api/airport/', {params: {ip : this.publicIp } } )
                  .then(response => {
                    this.closestAirportsInfo = response.data;
                  })
                  .catch(e => {
                    console.error(e);
                  });
              } finally {
                this.isFinding = false;
              }
            }
          }
        }
        , computed: {
        }
    }
</script>

<style>
    .rate .rate-item.set-on .icon, .rate .rate-item.set-half .is-half {
      color: DarkBlue;
    }
</style>