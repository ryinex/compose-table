/**
 * FilePreloadAPI Javascript File Preloading Toolkit
 * http://www.thecodearchives.appspot.com/raw/webkit%20audio/FilePreloadAPI.js
 *
 * Copyright 2012 Hunter John Larco
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
(function () {
	window.BlobBuilder = (function () {
		return window.BlobBuilder ||
			window.WebKitBlobBuilder ||
			window.MSBlobBuilder ||
			window.MozBlobBuilder
	})();
	if (!!(window.FileReader && window.XMLHttpRequest) && !!(window.Blob || window.BlobBuilder)) {
		var Preload = function Preload() {
			this.sample = (function PreloadSampler() { console.error('Internal Error: No file specified'); }).bind();
			this.read = (function PreloadReader() { console.error('Internal Error: No file specified'); }).bind();
			this.abort = (function PreloadAbort() { console.warn('Unable to abort: No file is being uploaded'); }).bind();
			this.open = (function PreloadOpener(url, type) {
				var type = type || 'data';
				this.sample = (function PreloadSampler() {
					var url = this.URL;
					var system = this.Preload;
					var xhr = new XMLHttpRequest();
					xhr.open('HEAD', url);
					xhr.onreadystatechange = (function (event) {
						if (this.xhr.readyState == this.xhr.DONE) {
							this.Preload.abort = (function PreloadAbort() { console.warn('Unable to abort: No file is being uploaded'); }).bind();
							var headers = this.xhr.getAllResponseHeaders().split('\n').slice(0, -1);
							var json = new Object();
							var i = headers.length; while (i--) {
								json[headers[i].slice(0, headers[i].indexOf(':'))] = headers[i].slice(headers[i].indexOf('\:') + 1).replace(/^\s*/, '');
							};
							try { this.Preload.onload(json); } catch (e) { };
						};
					}).bind({ 'Preload': system, 'xhr': xhr });
					this.Preload.abort = (function PreloadAbort() { this.xhr.abort(); try { this.Preload.onabort(this.Preload); } catch (e) { }; }).bind({ 'Preload': system, 'xhr': xhr });
					xhr.send();
				}).bind({ 'Preload': this, 'URL': url });
				this.read = (function PreloadReader() {
					var url = this.URL;
					var system = this.Preload;
					var xhr = new XMLHttpRequest(), blob, fileReader = new FileReader();
					xhr.open("GET", url, true);
					xhr.responseType = "arraybuffer";
					xhr.addEventListener("load", (function (event) {
						if (event.target.status === 200) {
							if (!!window.DataView) { var bufferArray = new DataView(xhr.response); } else { var bufferArray = xhr.response; };
							if (!!window.Blob) { blob = new Blob([bufferArray], { type: xhr.getResponseHeader('Content-Type') }); } else {
								blob = new BlobBuilder();
								blob.append(bufferArray);
								blob = blob.getBlob(xhr.getResponseHeader('Content-Type'));
							};
							fileReader.onload = (function (event) {
								this.readyState = 4;
								try { this.onreadystatechange(this); } catch (e) { };
								try { this.onload(PreloadLoadEvent(event.target.result, event.total, event.total)); } catch (e) { };
							}).bind(system);
							fileReader.addEventListener("error", (function (event) {
								try { this.onerror(system); } catch (e) { console.error('Preload.js Error: ' + e); };
							}).bind(system), false);
							fileReader.addEventListener("progress", (function (event) {
								if (event.lengthComputable) {
									var percentLoaded = event.loaded / event.total;
									if (percentLoaded < 1) {
										try { this.onprogress(PreloadProgressEvent(percentLoaded * 0.05 + 0.95, event.total, event.total)); } catch (e) { };
									};
								} else {
									try { this.onprogress(PreloadProgressEvent(undefined, event.total, event.total)); } catch (e) { };
								};
							}).bind(system), false);
							system.abort = (function PreloadAbort() { this.fileReader.abort(); try { this.Preload.onabort(this.Preload); } catch (e) { }; }).bind({ 'Preload': system, 'fileReader': fileReader });
							if (type === 'text') { fileReader.readAsText(blob); } else { fileReader.readAsDataURL(blob); };
						};
					}).bind(this), false);
					xhr.addEventListener("progress", (function (event) {
						var contentLength;

						if (event.lengthComputable) {
							contentLength = event.total;
						} else {
							contentLength = parseInt(event.target.getResponseHeader('x-decompressed-content-length'), 10);
						}
						var percentLoaded = event.loaded / contentLength;
						try { this.onprogress(PreloadProgressEvent(percentLoaded, event.loaded, contentLength)); } catch (e) { };
					}).bind(system), false);
					xhr.addEventListener("error", (function (event) {
						try { this.onerror(system); } catch (e) { console.error('Preload.js Error: ' + e); };
					}).bind(system), false);
					xhr.addEventListener("readystatechange", (function (event) {
						if (this.xhr.readyState != 4) {
							this.Preload.readyState = this.xhr.readyState;
							try { this.Preload.onreadystatechange(this.Preload); } catch (e) { };
						};
					}).bind({ 'Preload': system, 'xhr': xhr }), false);
					var PreloadProgressEvent = (function EventConstructor(progress, loadedBytes, totalBytes) {
						return new PreloadProgressEvent(this.Preload, this.URL, progress, loadedBytes, totalBytes);

						function PreloadProgressEvent(parent, target, progress, loadedBytes, totalBytes) {
							Object.defineProperty(this, "total", {
								value: totalBytes,
								writable: false, enumerable: true, configurable: false
							});
							Object.defineProperty(this, "parent", {
								value: parent,
								writable: false, enumerable: true, configurable: false
							});
							Object.defineProperty(this, "target", {
								value: target,
								writable: false, enumerable: true, configurable: false
							});
							Object.defineProperty(this, "progress", {
								value: progress,
								writable: false, enumerable: true, configurable: false
							});
							Object.defineProperty(this, "loaded", {
								value: loadedBytes,
								writable: false, enumerable: true, configurable: false
							});
							Object.defineProperty(this, "total", {
								value: totalBytes,
								writable: false, enumerable: true, configurable: false
							});
							Object.defineProperty(this, "size", {
								value: totalBytes,
								writable: false, enumerable: true, configurable: false
							});
						};
					}).bind({ 'Preload': system, 'URL': url });
					var PreloadLoadEvent = (function EventConstructor(response, loadedBytes, totalBytes) {
						return new PreloadLoadEvent(this.Preload, this.URL, 1, response, loadedBytes, totalBytes);
						function PreloadLoadEvent(parent, target, progress, response, loadedBytes, totalBytes) {
							Object.defineProperty(this, "parent", {
								value: parent,
								writable: false, enumerable: true, configurable: false
							});
							Object.defineProperty(this, "target", {
								value: target,
								writable: false, enumerable: true, configurable: false
							});
							Object.defineProperty(this, "progress", {
								value: progress,
								writable: false, enumerable: true, configurable: false
							});
							Object.defineProperty(this, "response", {
								value: response,
								writable: false, enumerable: true, configurable: false
							});
							Object.defineProperty(this, "loaded", {
								value: loadedBytes,
								writable: false, enumerable: true, configurable: false
							});
							Object.defineProperty(this, "size", {
								value: totalBytes,
								writable: false, enumerable: true, configurable: false
							});
						};
					}).bind({ 'Preload': system, 'URL': url });
					system.abort = (function PreloadAbort() { this.xhr.abort(); try { this.Preload.onabort(this.Preload); } catch (e) { }; }).bind({ 'Preload': system, 'xhr': xhr });
					xhr.send();
				}).bind({ 'Preload': this, 'URL': url, 'type': type });
			}).bind(this);
			this.about = {
				author: 'Hunter John Larco',
				version: 1
			};
			this.onload = null;
			this.onerror = null;
			this.onabort = null;
			this.onprogress = null;
			this.addEventListener = (function PreloadAddEventListener(name, funct, bool) {
				if (!!this['on' + name] || this['on' + name] === null) { this['on' + name] = funct; } else {
					console.warn('The on' + name + ' event doesn\'t exist in the Preload object');
				};
			}).bind(this);
			this.onreadystatechange = null;
			this.readyState = 0;
		};
		window.Preload = Preload;
	} else {
		console.error('Support needed for reading files is not present:\n\nnew FileReader(): ' + !!window.FileReader + '\nnew Blob(): ' + !!window.Blob + '\nnew BlobBuilder(): ' + !!window.BlobBuilder + '\nnew XMLHttpRequest(): ' + !!window.XMLHttpRequest);
	};
})();