<template>
    <section>
      <div class="card">
        <div class="card-content">
          <div class="content"  style="min-height: 35em;">
            <H1>Travel planner demo</H1>
            <div class="columns">
              <div class="column is-one-quarter">
                <b-field label="Select hotel zone" label-position="on-border">
                    <b-autocomplete
                        rounded
                        v-model="selectedZone"
                        :data="hotelZones"
                        :loading="isFetchingHotels"
                        @typing="getAsyncZones"
                        icon="magnify"
                        placeholder="Search hotel zone"
                        @select="updateSelectedZone"
                        >
                        <template slot-scope="props">
                          <div>{{ props.option.destination }} ({{ props.option.country }})</div>
                        </template>
                        <template #empty>
                          <span v-if="selectedZone && selectedZone.length > 4 && !isFetchingHotels">
                            No results for '{{selectedZone}}'
                          </span>
                        </template>
                    </b-autocomplete>
                </b-field>
              </div>
              <div class="column is-one-quarter">
                <b-rate custom-text="Adults" icon="human-greeting" v-model="adults"></b-rate>
                <b-rate custom-text="Children" icon="human-child" v-model="children"></b-rate>
                <b-button v-if="children > 0" label="reset children" size="is-small" icon-left="human-child" @click="() => children = 0"/>
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
                    <b-select placeholder="Airport" icon="airplane-takeoff" v-model="selectedAirport">
                        <option
                          v-for="distance in this.closestAirportsInfo.distances"
                          :value="distance.airport.iataFaaCode"
                        >
                          ({{ distance.airport.iataFaaCode }}) - <strong>{{ distance.airport.name }}</strong> (at {{ distance.distance.kilometers }}Km)
                        </option>
                    </b-select>
                </b-field>
              </div>
              <div class="column is-half">
                <div style="margin-bottom: 1em;">
                  Plans for {{ adults }} adults
                  <span v-if="children>0">
                    and {{ children }} children
                  </span>
                  <span v-if="selectedZoneName">
                    going to {{ selectedZoneName }}
                  </span>
                  <span v-if="from">
                    departing {{ $moment(from).format('L') }}
                    <span v-if="selectedAirport">
                      from {{ selectedAirport }}
                    </span>
                    <span v-if="to">
                      and comming back {{ $moment(to).format('L') }}
                    </span>
                  </span>
                </div>
                <b-button type="is-info" :disabled="!(selectedZoneName && from && to && selectedAirport)">
                  Show me!
                </b-button>
              </div>
            </div>
          </div> <!-- End content -->
        </div>
      </div>
    </section>
</template>

<script>
    import axios from "axios";
    import _ from 'lodash';
    import Bottleneck from "bottleneck";

    const limiter = new Bottleneck({
      maxConcurrent: 1
    });

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
                children: 0,
                selectedZone: null,
                selectedZoneName: null,
                isFetchingHotels: false,
                hotelZones: [],
                selectedAirport: null
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
          getAsyncZones: _.debounce(function (name) {
              this.isFetchingHotels = true
              if (!name.length || name.length < 4) {
                  this.hotelZones = [];
                  this.selectedZoneName = null;
                  return
              }
              limiter.schedule(async () => {
                await axios
                  .get('/travel-planner/api/hotel/',{ params: {term: name} })
                  .then(response => {
                    this.hotelZones.splice(0);
                    this.selectedZoneName = null;
                    response.data.forEach(hotelZone => {
                      this.hotelZones.push(hotelZone);
                    });
                  })
                  .catch(e => {
                    this.hotelZones = []
                    console.error(e);
                  })
                  .then( () => {
                    this.isFetchingHotels = false
                  });
              });
              this.isFetchingHotels = false
          }, 300),
          updateSelectedZone (option) {
            this.selectedZoneName = option.destination;
          },
          findClosestAirports () {
            if(!this.isFinding && this.publicIp) {
              this.isFinding = true;
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