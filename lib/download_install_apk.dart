import 'dart:async';

import 'package:flutter/services.dart';

class DownloadInstallApk {
  static const EventChannel _progressChannel =
      EventChannel('com.zhgwu.download.install.apk');
  Stream<Event>? _progressStream;

  Stream<Event> execute(String url) {
    final StreamController<Event> controller =
        StreamController<Event>.broadcast();
    if (_progressStream == null) {
      _progressChannel.receiveBroadcastStream(
        <dynamic, dynamic>{
          'url': url,
        },
      ).listen((dynamic event) {
        final Event otaEvent = _toEvent(event.cast<String>());
        controller.add(otaEvent);
      }).onError((Object error) {
        if (error is PlatformException) {
          controller.add(_toEvent(<String?>[error.code, error.message]));
        }
      });
      _progressStream = controller.stream;
    }
    return _progressStream!;
  }

  Event _toEvent(List<String?> event) {
    return Event(Status.values[int.parse(event[0]!)], event[1]);
  }
}

class Event {
  Event(this.status, this.value);
  Status status;
  String? value;

  @override
  String toString() {
    return 'Event{status: $status, value: $value}';
  }
}

enum Status {
  downloading,
  downloadError,
  installing,
  installError,
  alreadyRunningError,
}
