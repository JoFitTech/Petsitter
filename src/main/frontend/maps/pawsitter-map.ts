import * as L from 'leaflet';
import type { LatLngExpression, Map as LeafletMap, Marker, TileLayer } from 'leaflet';
import 'leaflet/dist/leaflet.css';

type PawsitterMapState = {
  map: LeafletMap;
  marker?: Marker;
  tileLayer: TileLayer;
};

type PawsitterMapApi = {
  showDefault: (element: HTMLElement) => void;
  showLocation: (element: HTMLElement, latitude: number, longitude: number, label: string) => void;
  destroy: (element: HTMLElement) => void;
};

declare global {
  interface Window {
    PawsitterMap?: PawsitterMapApi;
  }
}

const DEFAULT_CENTER: LatLngExpression = [51.1657, 10.4515];
const DEFAULT_ZOOM = 6;
const LOCATION_ZOOM = 12;
const states = new WeakMap<HTMLElement, PawsitterMapState>();

const originIcon = L.divIcon({
  className: 'pawsitter-origin-marker',
  html: '<span></span>',
  iconSize: [28, 36],
  iconAnchor: [14, 34],
  popupAnchor: [0, -30],
});

function ensureMarkerStyles(): void {
  if (document.getElementById('pawsitter-map-marker-styles')) {
    return;
  }

  const style = document.createElement('style');
  style.id = 'pawsitter-map-marker-styles';
  style.textContent = `
    .pawsitter-origin-marker {
      background: transparent;
      border: 0;
    }

    .pawsitter-origin-marker span {
      width: 24px;
      height: 24px;
      background: #4a3428;
      border: 3px solid #ffffff;
      border-radius: 50% 50% 50% 0;
      box-shadow: 0 6px 16px rgba(74, 52, 40, 0.38);
      display: block;
      transform: rotate(-45deg);
    }

    .pawsitter-origin-marker span::after {
      content: "";
      width: 8px;
      height: 8px;
      background: #ffffff;
      border-radius: 50%;
      left: 8px;
      position: absolute;
      top: 8px;
    }
  `;
  document.head.appendChild(style);
}

function ensureMap(element: HTMLElement): PawsitterMapState {
  ensureMarkerStyles();

  const existingState = states.get(element);
  if (existingState) {
    return existingState;
  }

  const map = L.map(element, {
    attributionControl: true,
    scrollWheelZoom: false,
    zoomControl: true,
  }).setView(DEFAULT_CENTER, DEFAULT_ZOOM);

  const tileLayer = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19,
  }).addTo(map);

  const state = { map, tileLayer };
  states.set(element, state);
  invalidateSoon(map);
  return state;
}

function invalidateSoon(map: LeafletMap): void {
  window.setTimeout(() => map.invalidateSize(), 0);
  window.setTimeout(() => map.invalidateSize(), 250);
}

function clearMarker(state: PawsitterMapState): void {
  if (state.marker) {
    state.marker.remove();
    state.marker = undefined;
  }
}

function showDefault(element: HTMLElement): void {
  const state = ensureMap(element);
  clearMarker(state);
  state.map.setView(DEFAULT_CENTER, DEFAULT_ZOOM);
  invalidateSoon(state.map);
}

function showLocation(element: HTMLElement, latitude: number, longitude: number, label: string): void {
  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
    showDefault(element);
    return;
  }

  const state = ensureMap(element);
  const coordinates: LatLngExpression = [latitude, longitude];
  clearMarker(state);

  const popupContent = document.createElement('strong');
  popupContent.textContent = label;

  state.marker = L.marker(coordinates, { icon: originIcon })
    .addTo(state.map)
    .bindPopup(popupContent);
  state.map.setView(coordinates, LOCATION_ZOOM);
  invalidateSoon(state.map);
}

function destroy(element: HTMLElement): void {
  const state = states.get(element);
  if (!state) {
    return;
  }

  state.map.remove();
  states.delete(element);
}

window.PawsitterMap = {
  destroy,
  showDefault,
  showLocation,
};
