import Cropper from 'cropperjs';

type CropHost = HTMLElement & { pawsitterCropper?: Cropper };

const template = `
  <cropper-canvas background>
    <cropper-image rotatable scalable skewable translatable></cropper-image>
    <cropper-shade hidden></cropper-shade>
    <cropper-handle action="select" plain></cropper-handle>
    <cropper-selection initial-coverage="0.72" aspect-ratio="1" movable resizable zoomable outlined>
      <cropper-grid role="grid" bordered covered></cropper-grid>
      <cropper-crosshair centered></cropper-crosshair>
      <cropper-handle action="move" theme-color="rgba(255, 255, 255, 0.35)"></cropper-handle>
      <cropper-handle action="n-resize"></cropper-handle>
      <cropper-handle action="e-resize"></cropper-handle>
      <cropper-handle action="s-resize"></cropper-handle>
      <cropper-handle action="w-resize"></cropper-handle>
      <cropper-handle action="ne-resize"></cropper-handle>
      <cropper-handle action="nw-resize"></cropper-handle>
      <cropper-handle action="se-resize"></cropper-handle>
      <cropper-handle action="sw-resize"></cropper-handle>
    </cropper-selection>
  </cropper-canvas>
`;

function destroy(host: CropHost): void {
  host.pawsitterCropper?.destroy();
  delete host.pawsitterCropper;
}

async function attach(host: CropHost, source: string): Promise<boolean> {
  destroy(host);
  host.replaceChildren();
  const image = document.createElement('img');
  image.src = source;
  await image.decode();
  host.append(image);
  host.pawsitterCropper = new Cropper(image, { container: host, template });

  await Promise.all([
    customElements.whenDefined('cropper-canvas'),
    customElements.whenDefined('cropper-selection'),
  ]);
  await new Promise<void>((resolve) => {
    requestAnimationFrame(() => resolve());
  });

  const canvas = host.querySelector('cropper-canvas') as HTMLElement | null;
  const selection = host.querySelector('cropper-selection') as HTMLElement | null;
  if (!canvas || !selection) {
    throw new Error('Der Bildausschnitt konnte nicht initialisiert werden.');
  }
  canvas.style.width = '100%';
  canvas.style.height = '100%';
  selection.style.borderRadius = '50%';
  selection.style.overflow = 'hidden';
  return true;
}

async function exportCrop(host: CropHost): Promise<string> {
  const selection = host.pawsitterCropper?.getCropperSelection();
  if (!selection) {
    throw new Error('Kein Bildausschnitt ausgewählt.');
  }
  const canvas = await selection.$toCanvas({ width: 1024, height: 1024 });
  return canvas.toDataURL('image/jpeg', 0.92);
}

declare global {
  interface Window {
    PawsitterImageCrop: {
      attach: typeof attach;
      destroy: typeof destroy;
      exportCrop: typeof exportCrop;
    };
  }
}

window.PawsitterImageCrop = { attach, destroy, exportCrop };
