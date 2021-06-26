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
                  <span v-if="selectedDestinationName">
                    going to {{ selectedDestinationName }}
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
                >
                <b-table-column field="name" label="Hotel name" width="40%" sortable searchable>
                  <template v-slot="props">
                    <!--
                    <b-icon v-if="props.row.visible" icon="eye" type="is-success" />
                    -->
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

                <b-table-column field="minRate" label="Mininum rate" width="10%" sortable>
                  <template v-slot="props">
                    {{ props.row.minRate }}
                  </template>
                </b-table-column>

                <b-table-column field="maxRate" label="Max rate" width="10%" sortable>
                  <template v-slot="props">
                    {{ props.row.maxRate }}
                  </template>
                </b-table-column>
            </b-table>
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
                selectedDestination: null,
                selectedDestinationCode: null,
                selectedDestinationName: null,
                isFetchingZones: false,
                hotelDestinations: [],
                selectedAirport: null,
                isFetchingHotels: false,
                //availableHotels: []
                availableHotels: [{"code":725,"name":"Manaus","categoryName":"3 STARS","zoneName":"S'Arenal","latitude":"39.4971850225495","longitude":"2.75442615151405","minRate":300.00,"maxRate":502.68,"rooms":[{"code":"DBT.ST","name":"Double or Twin STANDARD","rates":[{"rateKey":"20210721|20210725|W|1|725|DBT.ST|OP-FIT|HB||1~2~0||N@06~~21818a~-2100678115~N~~~NOR~3FB394DCBEBB427162455025708900AAUK0000001000000000121818a","rateClass":"NOR","net":394.24,"boardCode":"HB","boardName":"HALF BOARD"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|CG-FIT|RO||1~2~0||N@06~~24813c~1874844477~N~~~NOR~3FB394DCBEBB427162455025708900AAUK0000001000000000124813c","rateClass":"NOR","net":316.72,"boardCode":"RO","boardName":"ROOM ONLY"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|NRF-FIT|BB||1~2~0||N@06~~234145~-1023973571~N~~~NRF~3FB394DCBEBB427162455025708900AAUK00000010000000001234145","rateClass":"NRF","net":325.52,"boardCode":"BB","boardName":"BED AND BREAKFAST"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|CG-PVP|BB||1~2~0||N@06~~2001c2~-1771666455~S~~~NOR~3FB394DCBEBB427162455025708900AAUK0000001000000000125218a","rateClass":"NOR","net":394.82,"boardCode":"BB","boardName":"BED AND BREAKFAST"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|NRF-FIT|HB||1~2~0||N@06~~21818a~1120480951~N~~~NRF~3FB394DCBEBB427162455025708900AAUK0000001000000000121818a","rateClass":"NRF","net":394.24,"boardCode":"HB","boardName":"HALF BOARD"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|CG-PVPHB|HB||1~2~0||N@06~~20021c~1896000243~S~~~NOR~3FB394DCBEBB427162455025708900AAUK000000100000000012501d9","rateClass":"NOR","net":473.80,"boardCode":"HB","boardName":"HALF BOARD"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|NRF-PVPHB|HB||1~2~0||N@06~~200201~1777519856~S~~~NRF~3FB394DCBEBB427162455025708900AAUK000000100000000012261c0","rateClass":"NRF","net":448.38,"boardCode":"HB","boardName":"HALF BOARD"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|OP-FIT|BB||1~2~0||N@06~~234145~49834659~N~~~NOR~3FB394DCBEBB427162455025708900AAUK00000010000000001234145","rateClass":"NOR","net":325.52,"boardCode":"BB","boardName":"BED AND BREAKFAST"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|NRF-PVPBB|BB||1~2~0||N@06~~2321ab~-295446263~S~~~NRF~3FB394DCBEBB427162455025708900AAUK00000010000000001240175","rateClass":"NRF","net":373.64,"boardCode":"BB","boardName":"BED AND BREAKFAST"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|CG-FIT|HB||1~2~0||N@06~~23819e~-916531942~N~~~NOR~3FB394DCBEBB427162455025708900AAUK0000001000000000123819e","rateClass":"NOR","net":414.56,"boardCode":"HB","boardName":"HALF BOARD"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|CG-FIT|BB||1~2~0||N@06~~218156~-141523584~N~~~NOR~3FB394DCBEBB427162455025708900AAUK00000010000000001218156","rateClass":"NOR","net":342.24,"boardCode":"BB","boardName":"BED AND BREAKFAST"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|OP-FIT|RO||1~2~0||N@06~~20012c~2094027776~N~~~NOR~3FB394DCBEBB427162455025708900AAUK0000001000000000120012c","rateClass":"NOR","net":300.00,"boardCode":"RO","boardName":"ROOM ONLY"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST|NRF-FIT|RO||1~2~0||N@06~~20012c~1020219546~N~~~NRF~3FB394DCBEBB427162455025708900AAUK0000001000000000120012c","rateClass":"NRF","net":300.00,"boardCode":"RO","boardName":"ROOM ONLY"}]},{"code":"DBL.ST","name":"DOUBLE STANDARD","rates":[{"rateKey":"20210721|20210725|W|1|725|DBL.ST|NRF-PVPHB|HB||1~2~0||N@06~~200201~1303753960~S~~~NRF~3FB394DCBEBB427162455025708900AAUK000000100000000012261c0","rateClass":"NRF","net":448.38,"boardCode":"HB","boardName":"HALF BOARD"},{"rateKey":"20210721|20210725|W|1|725|DBL.ST|NRF-PVPBB|BB||1~2~0||N@06~~2321ab~-769212159~S~~~NRF~3FB394DCBEBB427162455025708900AAUK00000010000000001240175","rateClass":"NRF","net":373.64,"boardCode":"BB","boardName":"BED AND BREAKFAST"},{"rateKey":"20210721|20210725|W|1|725|DBL.ST|CG-PVP|BB||1~2~0||N@06~~2001c2~1400200945~S~~~NOR~3FB394DCBEBB427162455025708900AAUK0000001000000000125218a","rateClass":"NOR","net":394.82,"boardCode":"BB","boardName":"BED AND BREAKFAST"},{"rateKey":"20210721|20210725|W|1|725|DBL.ST|CG-FIT|RO||1~2~0||N@06~~234145~-212631099~N~~~NOR~3FB394DCBEBB427162455025708900AAUK00000010000000001234145","rateClass":"NOR","net":325.52,"boardCode":"RO","boardName":"ROOM ONLY"},{"rateKey":"20210721|20210725|W|1|725|DBL.ST|CG-FIT|BB||1~2~0||N@06~~20415f~-97171544~N~~~NOR~3FB394DCBEBB427162455025708900AAUK0000001000000000120415f","rateClass":"NOR","net":351.04,"boardCode":"BB","boardName":"BED AND BREAKFAST"},{"rateKey":"20210721|20210725|W|1|725|DBL.ST|CG-FIT|HB||1~2~0||N@06~~2241a7~274685186~N~~~NOR~3FB394DCBEBB427162455025708900AAUK000000100000000012241a7","rateClass":"NOR","net":423.36,"boardCode":"HB","boardName":"HALF BOARD"}]},{"code":"DBT.ST-1","name":"double room woods view 2  adults","rates":[{"rateKey":"20210721|20210725|W|1|725|DBT.ST-1|CG-PVP|BB||1~2~0||N@06~~2001e6~-749635792~S~~~NOR~3FB394DCBEBB427162455025708900AAUK0000001000000000122a1aa","rateClass":"NOR","net":426.42,"boardCode":"BB","boardName":"BED AND BREAKFAST"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST-1|NRF-PVPHB|HB||1~2~0||N@06~~25a215~-751978178~S~~~NRF~3FB394DCBEBB427162455025708900AAUK000000100000000012401d2","rateClass":"NRF","net":466.64,"boardCode":"HB","boardName":"HALF BOARD"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST-1|NRF-PVPBB|BB||1~2~0||N@06~~2461cd~404108349~S~~~NRF~3FB394DCBEBB427162455025708900AAUK00000010000000001236193","rateClass":"NRF","net":403.54,"boardCode":"BB","boardName":"BED AND BREAKFAST"},{"rateKey":"20210721|20210725|W|1|725|DBT.ST-1|CG-PVPHB|HB||1~2~0||N@06~~200232~-1260107929~S~~~NOR~3FB394DCBEBB427162455025708900AAUK000000100000000012081ed","rateClass":"NOR","net":493.08,"boardCode":"HB","boardName":"HALF BOARD"}]},{"code":"SUI.ST","name":"SUITE STANDARD","rates":[{"rateKey":"20210721|20210725|W|1|725|SUI.ST|CG-FIT|HB||1~2~0||N@06~~2441f6~1137356937~N~~~NOR~3FB394DCBEBB427162455025708900AAUK000000100000000012441f6","rateClass":"NOR","net":502.68,"boardCode":"HB","boardName":"HALF BOARD"},{"rateKey":"20210721|20210725|W|1|725|SUI.ST|CG-FIT|BB||1~2~0||N@06~~2241ae~-250774385~N~~~NOR~3FB394DCBEBB427162455025708900AAUK000000100000000012241ae","rateClass":"NOR","net":430.36,"boardCode":"BB","boardName":"BED AND BREAKFAST"},{"rateKey":"20210721|20210725|W|1|725|SUI.ST|CG-FIT|RO||1~2~0||N@06~~254194~650040652~N~~~NOR~3FB394DCBEBB427162455025708900AAUK00000010000000001254194","rateClass":"NOR","net":404.84,"boardCode":"RO","boardName":"ROOM ONLY"}]}]}],
                isPaginated: true,
                isPaginationSimple: true,
                paginationPosition: 'top',
                defaultSortDirection: 'asc',
                sortIcon: 'arrow-up',
                sortIconSize: 'is-small',
                currentPage: 1,
                perPage: 10,
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
          updateSelectedZone (option) {
            this.selectedDestinationName = option.destination;
            this.selectedDestinationCode = option.code;
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
          },
          findAvailableHotels () {
            this.availableHotels.splice(0);
            this.isFetchingHotels = true
            console.error('Finding hotels...');
            if (this.from && this.to && this.selectedDestinationCode) {
              limiter.schedule(async () => {
                console.error('Launching request');
                await axios
                  .get('/travel-planner/api/hotel/availability',{ params: {
                    checkIn: this.$moment(this.from).format("YYYY-MM-DD")
                    , checkOut: this.$moment(this.to).format("YYYY-MM-DD")
                    , destinationId: this.selectedDestinationCode
                    , numAdults: this.adults
                    , numChildren: this.children
                  } })
                  .then(response => {
                    console.error('Processing response');
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