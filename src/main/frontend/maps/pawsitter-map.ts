import * as L from 'leaflet';
import type { LayerGroup, LatLngBoundsExpression, LatLngExpression, Map as LeafletMap, Marker, TileLayer } from 'leaflet';
import 'leaflet/dist/leaflet.css';

type PawsitterMapState = {
  map: LeafletMap;
  offerLayer: LayerGroup;
  originMarker?: Marker;
  tileLayer: TileLayer;
};

type MapLocation = {
  latitude: number;
  longitude: number;
  placeName?: string | null;
  postalCode?: string | null;
};

type OfferMapLocation = MapLocation & {
  offerId: string;
  title?: string | null;
};

type SearchMapPayload = {
  offers?: OfferMapLocation[] | null;
  origin?: MapLocation | null;
};

type PawsitterMapApi = {
  showDefault: (element: HTMLElement) => void;
  showLocation: (element: HTMLElement, latitude: number, longitude: number, label: string) => void;
  showSearchResults: (element: HTMLElement, payload: SearchMapPayload) => void;
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
const RESULTS_MAX_ZOOM = 12;
const states = new WeakMap<HTMLElement, PawsitterMapState>();

const originIcon = L.divIcon({
  className: 'pawsitter-origin-marker',
  html: '<span></span>',
  iconSize: [28, 36],
  iconAnchor: [14, 34],
  popupAnchor: [0, -30],
});

const offerIcon = L.divIcon({
  className: 'pawsitter-offer-marker',
  html: '<span></span>',
  iconSize: [22, 22],
  iconAnchor: [11, 11],
  popupAnchor: [0, -14],
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

    .pawsitter-offer-marker {
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
      position: relative;
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

    .pawsitter-offer-marker span {
      width: 18px;
      height: 18px;
      background: #7b5236;
      border: 3px solid #ffffff;
      border-radius: 50%;
      box-shadow: 0 4px 12px rgba(74, 52, 40, 0.32);
      display: block;
    }

    .pawsitter-map-popup {
      color: #4a3428;
      font-family: Inter, Arial, sans-serif;
      min-width: 150px;
    }

    .pawsitter-map-popup strong {
      display: block;
      font-size: 13px;
      margin-bottom: 4px;
    }

    .pawsitter-map-popup span {
      color: #7b7069;
      display: block;
      font-size: 12px;
      line-height: 1.35;
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
  const offerLayer = L.layerGroup().addTo(map);

  const state = { map, offerLayer, tileLayer };
  states.set(element, state);
  invalidateSoon(map);
  return state;
}

function invalidateSoon(map: LeafletMap): void {
  window.setTimeout(() => map.invalidateSize(), 0);
  window.setTimeout(() => map.invalidateSize(), 250);
}

function clearMarker(state: PawsitterMapState): void {
  if (state.originMarker) {
    state.originMarker.remove();
    state.originMarker = undefined;
  }
  state.offerLayer.clearLayers();
}

function showDefault(element: HTMLElement): void {
  const state = ensureMap(element);
  clearMarker(state);
  state.map.setView(DEFAULT_CENTER, DEFAULT_ZOOM);
  invalidateSoon(state.map);
}

function showLocation(element: HTMLElement, latitude: number, longitude: number, label: string): void {
  showSearchResults(element, {
    origin: {
      latitude,
      longitude,
      placeName: label,
    },
    offers: [],
  });
}

function showSearchResults(element: HTMLElement, payload: SearchMapPayload): void {
  const state = ensureMap(element);
  clearMarker(state);

  const bounds: LatLngExpression[] = [];
  const origin = payload?.origin ?? null;
  if (isValidLocation(origin)) {
    const originCoordinates: LatLngExpression = [origin.latitude, origin.longitude];
    state.originMarker = L.marker(originCoordinates, { icon: originIcon })
      .addTo(state.map)
      .bindPopup(buildOriginPopup(origin));
    bounds.push(originCoordinates);
  }

  groupOffers(payload?.offers ?? []).forEach((group) => {
    const coordinates: LatLngExpression = [group.latitude, group.longitude];
    L.marker(coordinates, { icon: offerIcon })
      .addTo(state.offerLayer)
      .bindPopup(buildOfferPopup(group));
    bounds.push(coordinates);
  });

  applyView(state.map, bounds);
  invalidateSoon(state.map);
}

function applyView(map: LeafletMap, bounds: LatLngExpression[]): void {
  if (bounds.length === 0) {
    map.setView(DEFAULT_CENTER, DEFAULT_ZOOM);
    return;
  }
  if (bounds.length === 1) {
    map.setView(bounds[0], LOCATION_ZOOM);
    return;
  }

  map.fitBounds(bounds as LatLngBoundsExpression, {
    maxZoom: RESULTS_MAX_ZOOM,
    padding: [32, 32],
  });
}

function isValidLocation(location: MapLocation | null | undefined): location is MapLocation {
  return location !== null
    && location !== undefined
    && Number.isFinite(location.latitude)
    && Number.isFinite(location.longitude);
}

function groupOffers(offers: OfferMapLocation[]): OfferLocationGroup[] {
  const groups = new Map<string, OfferLocationGroup>();
  offers.filter(isValidLocation).forEach((offer) => {
    const key = [
      offer.latitude.toFixed(6),
      offer.longitude.toFixed(6),
      offer.postalCode ?? '',
    ].join(':');
    const existingGroup = groups.get(key);
    if (existingGroup) {
      existingGroup.offers.push(offer);
      return;
    }

    groups.set(key, {
      latitude: offer.latitude,
      longitude: offer.longitude,
      placeName: offer.placeName,
      postalCode: offer.postalCode,
      offers: [offer],
    });
  });
  return Array.from(groups.values());
}

type OfferLocationGroup = MapLocation & {
  offers: OfferMapLocation[];
};

function buildOriginPopup(origin: MapLocation): HTMLElement {
  return buildPopup('Ausgangspunkt', formatLocation(origin));
}

function buildOfferPopup(group: OfferLocationGroup): HTMLElement {
  const title = group.offers.length === 1
    ? group.offers[0].title || 'Angebot'
    : `${group.offers.length} Angebote`;
  const details = [
    formatLocation(group),
    ...group.offers.slice(0, 4).map((offer) => offer.title).filter(hasText),
  ];
  if (group.offers.length > 4) {
    details.push(`+ ${group.offers.length - 4} weitere`);
  }
  return buildPopup(title, details);
}

function buildPopup(title: string, details: string | string[]): HTMLElement {
  const popup = document.createElement('div');
  popup.className = 'pawsitter-map-popup';

  const heading = document.createElement('strong');
  heading.textContent = title;
  popup.appendChild(heading);

  const detailList = Array.isArray(details) ? details : [details];
  detailList.filter(hasText).forEach((detail) => {
    const line = document.createElement('span');
    line.textContent = detail;
    popup.appendChild(line);
  });

  return popup;
}

function formatLocation(location: MapLocation): string {
  return [location.postalCode, location.placeName]
    .filter(hasText)
    .join(' ');
}

function hasText(value: string | null | undefined): value is string {
  return Boolean(value && value.trim().length > 0);
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
  showSearchResults,
};
