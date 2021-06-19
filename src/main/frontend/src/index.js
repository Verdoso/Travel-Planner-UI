import 'buefy/dist/buefy.css'
import 'bootstrap/dist/css/bootstrap.css';
import '@mdi/font/css/materialdesignicons.css'

import './less/travel-planner.less';

import Vue from 'vue'
import Buefy from 'buefy'

Vue.use(Buefy)


import travelPlanner from './vue/travel-planner.vue';
import Moment from 'moment';

Vue.component('travel-planner', travelPlanner)

Moment.locale( 'en' );
Object.defineProperty( Vue.prototype, '$moment', { value: Moment });

var app = new Vue({
  el: '#app',
  data() {
    return {
    }
  },
  mounted() {
    document.getElementById("loader").style.display = "none";
    document.getElementById("app").style.display = "block";
  }
})