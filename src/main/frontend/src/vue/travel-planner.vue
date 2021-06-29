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
                        v-model="selectedDestination"
                        :data="hotelDestinations"
                        :loading="isFetchingZones"
                        @typing="getAsyncZones"
                        icon="magnify"
                        placeholder="Search hotel zone"
                        @select="updateSelectedZone"
                        >
                        <template slot-scope="props">
                          <div>{{ props.option.destination }} ({{ props.option.country }})</div>
                        </template>
                        <template #empty>
                          <span v-if="selectedDestination && selectedDestination.length > 4 && !isFetchingZones">
                            No results for '{{selectedDestination}}'
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
                        :focused-date="from"
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
              <div class="column">
                <div style="margin-bottom: 1em;">
                  Plans for {{ adults }} adults
                  <span v-if="children>0">
                    and {{ children }} children
                  </span>
                  <span v-if="selectedDestinationName">
                    going to <strong>{{ selectedDestinationName }}</strong>
                  </span>
                  <span v-if="from">
                    departing <strong>{{ $moment(from).format('L') }}</strong>
                    <span v-if="selectedAirport">
                      from <strong>{{ selectedAirport }}</strong>
                    </span>
                    <span v-if="selectedDestinationAirport">
                      landing at <strong>{{ selectedDestinationAirport }}</strong>
                    </span>
                    <span v-if="selectedHotel">
                      staying at the hotel <strong>{{ selectedHotel.name }} in {{ selectedHotel.zoneName }} ({{selectedHotel.minRate}}-{{selectedHotel.maxRate}} €)</strong>
                    </span>
                    <span v-if="to">
                      and comming back <strong>{{ $moment(to).format('L') }}</strong>
                    </span>
                  </span>
                </div>
                <b-button
                  type="is-info"
                  :disabled="!(selectedDestinationName && from && to)"
                  @click="findAvailableHotels"
                  :loading="isFetchingHotels"
                  >
                  Show me!
                </b-button>
              </div>
            </div>
            <b-table
                 ref="table"
                :data="availableHotels"
                :paginationSimple="isPaginationSimple"
                :paginated="isPaginated"
                :pagination-position="paginationPosition"
                :per-page="perPage"
                default-sort="minRate"
                :default-sort-direction="defaultSortDirection"
                v-show="availableHotels.length > 0 && !selectedHotel"
                >
                <b-table-column field="name" label="Hotel name" width="40%" sortable searchable>
                  <template v-slot="props">
                    {{ props.row.name }}
                  </template>
                </b-table-column>

                <b-table-column field="zoneName" label="Zone" width="20%" sortable searchable>
                  <template v-slot="props">
                    {{ props.row.zoneName }}
                  </template>
                </b-table-column>

                <b-table-column field="categoryName" label="Category" width="20%" sortable searchable>
                  <template v-slot="props">
                    {{ props.row.categoryName }}
                  </template>
                </b-table-column>

                <b-table-column field="minRate" label="Mininum rate" width="5%" sortable>
                  <template v-slot="props">
                    {{ props.row.minRate }}
                  </template>
                </b-table-column>

                <b-table-column field="maxRate" label="Max rate" width="5%" sortable>
                  <template v-slot="props">
                    {{ props.row.maxRate }}
                  </template>
                </b-table-column>

                <b-table-column field="name" label="Select" width="10%">
                  <template v-slot="props">
                    <b-button type="is-info" @click="() => updateSelectedHotel(props.row)">Select this</b-button>
                  </template>
                </b-table-column>
            </b-table>
            <div class="columns" v-if="selectedHotel">
              <!-- Search by IP
              <div class="column is-one-quarter">
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
               -->
              <div class="column is-half">
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
                <b-field v-if="this.closestDestinationAirportsInfo" label="To" horizontal>
                    <b-select placeholder="Airport" icon="airplane-landing" v-model="selectedDestinationAirport">
                        <option
                          v-for="distance in this.closestDestinationAirportsInfo.distances"
                          :value="distance.airport.iataFaaCode"
                        >
                          ({{ distance.airport.iataFaaCode }}) - <strong>{{ distance.airport.name }}</strong> (at {{ distance.distance.kilometers }}Km)
                        </option>
                    </b-select>
                    <p class="control">
                      <b-button type="is-info" @click="findFlight">Find a flight!</b-button>
                    </p>
                </b-field>
              </div>
            </div>
            <div class="columns" v-if="flightInfo.length > 0">
              <div class="column is-half">
                Flight options:
                <ul>
                <li v-for="flight in flightInfo":key="flight.id">
                  <div>
                    <strong>Departure</strong> from {{ flight.origin }} on {{ $moment(flight.departureDate).format("YYYY-MM-DD") }} at {{ $moment(flight.departureDate).format("hh:ss") }}, travelling with {{ flight.departureCarrier  }}
                  </div>
                  <div>
                    <strong>Return trip</strong> from {{ flight.destination }} on {{ $moment(flight.returnDate).format("YYYY-MM-DD") }} at {{ $moment(flight.returnDate).format("hh:ss") }}, travelling with {{ flight.returnCarrier }}
                  </div>
                  <div>
                    <strong>Total price</strong> {{ flight.minPrice }} €
                  </div>
                </li>
                </ul>
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
                closestDestinationAirportsInfo : null,
                from: null,
                to: null,
                adults: 2,
                children: 0,
                selectedDestination: null,
                selectedDestinationCode: null,
                selectedDestinationName: null,
                isFetchingZones: false,
                hotelDestinations: [],
                selectedAirport: null,
                selectedDestinationAirport: null,
                isFetchingHotels: false,
                availableHotels: [],
                isPaginated: true,
                isPaginationSimple: true,
                paginationPosition: 'top',
                defaultSortDirection: 'asc',
                sortIcon: 'arrow-up',
                sortIconSize: 'is-small',
                currentPage: 1,
                perPage: 10,
                selectedHotel: null,
                flightInfo: []
            }
        }
        , mounted() {
            axios
              .get('https://api64.ipify.org?format=json')
              .then(response => {
                this.publicIp = response.data.ip;
                this.findClosestAirports();
              })
              .catch(e => {
                console.error(e);
              });
        }
        , methods: {
          findFlight() {
            if(!this.isFinding && this.selectedHotel) {
              this.isFinding = true;
              this.flightInfo.splice(0);
              try {
                axios
                  .get('/travel-planner/api/flight/search/'
                    + this.closestAirportsInfo.distances.filter(distance => distance.airport.iataFaaCode === this.selectedAirport)[0].airport.countryCode
                    + '/EUR/en-US/'
                    + this.selectedAirport
                    + '/'
                    + this.selectedDestinationAirport
                    + '/'
                    + this.$moment(this.from).format('YYYY-MM-DD')
                    + '/'
                    + this.$moment(this.to).format('YYYY-MM-DD')
                    )
                  .then(response => {
                    response.data.forEach(flight => {
                      this.flightInfo.push(flight);
                    });
                    if(this.flightInfo.length===0) {
                      this.$buefy.toast.open({
                          duration: 2000,
                          message: `No flight found for the given dates and airports`,
                          position: 'is-bottom',
                          type: 'is-danger'
                      });
                    }
                  })
                  .catch(e => {
                    console.error(e);
                  });
              } finally {
                this.isFinding = false;
                this.print();
              }
            }
          },
          getAsyncZones: _.debounce(function (name) {
              this.isFetchingZones = true
              if (!name.length || name.length < 4) {
                  this.hotelDestinations = [];
                  this.selectedDestinationCode = null;
                  this.selectedDestinationName = null;
                  return
              }
              limiter.schedule(async () => {
                await axios
                  .get('/travel-planner/api/hotel/destinations',{ params: {term: name} })
                  .then(response => {
                    this.hotelDestinations.splice(0);
                    this.selectedDestinationCode = null;
                    this.selectedDestinationName = null;
                    response.data.forEach(hotelDestination => {
                      this.hotelDestinations.push(hotelDestination);
                    });
                  })
                  .catch(e => {
                    this.hotelDestinations = []
                    console.error(e);
                  })
                  .then( () => {
                    this.isFetchingZones = false
                  });
              });
              this.isFetchingZones = false
          }, 300),
          updateSelectedHotel (option) {
            this.selectedHotel = option;
            if(!this.isFinding && this.selectedHotel) {
              this.isFinding = true;
              try {
                axios
                  .get('/travel-planner/api/airport/by_position/', {params: {latitude : this.selectedHotel.latitude, longitude : this.selectedHotel.longitude } } )
                  .then(response => {
                    this.closestDestinationAirportsInfo = response.data;
                    this.selectedDestinationAirport = this.closestDestinationAirportsInfo.distances[0].airport.iataFaaCode;
                  })
                  .catch(e => {
                    console.error(e);
                  });
              } finally {
                this.isFinding = false;
              }
            }
          },
          updateSelectedZone (option) {
            this.selectedDestinationName = option.destination;
            this.selectedDestinationCode = option.code;
            this.availableHotels.splice(0);
            this.closestDestinationAirportsInfo = null
            this.selectedDestinationAirport = null;
            this.selectedHotel = null;
            this.flightInfo.splice(0);
          },
          findClosestAirports () {
            if(!this.isFinding && this.publicIp) {
              this.isFinding = true;
              try {
                axios
                  .get('/travel-planner/api/airport/by_ip/', {params: {ip : this.publicIp } } )
                  .then(response => {
                    this.closestAirportsInfo = response.data;
                    this.selectedAirport = this.closestAirportsInfo.distances[0].airport.iataFaaCode;
                  })
                  .catch(e => {
                    console.error(e);
                  });
              } finally {
                this.isFinding = false;
              }
            }
          },
          findAvailableHotels () {
            this.availableHotels.splice(0);
            this.flightInfo.splice(0);
            this.isFetchingHotels = true
            console.debug('Finding hotels...');
            if (this.from && this.to && this.selectedDestinationCode) {
              limiter.schedule(async () => {
                await axios
                  .get('/travel-planner/api/hotel/availability',{ params: {
                    checkIn: this.$moment(this.from).format("YYYY-MM-DD")
                    , checkOut: this.$moment(this.to).format("YYYY-MM-DD")
                    , destinationId: this.selectedDestinationCode
                    , numAdults: this.adults
                    , numChildren: this.children
                  } })
                  .then(response => {
                    this.availableHotels.splice(0);
                    response.data.forEach(hotel => {
                      this.availableHotels.push(hotel);
                    });
                  })
                  .catch(e => {
                    this.availableHotels = []
                    console.error(e);
                  })
                  .then( () => {
                    this.isFetchingHotels = false
                  });
              });
            } else {
              console.error('Something is missing: ' + this.from);
              console.error('Something is missing: ' + this.to);
              console.error('Something is missing: ' + this.selectedDestinationCode);
            }
            this.isFetchingHotels = false
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
