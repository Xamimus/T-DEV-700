import 'dart:ffi';
import 'dart:developer';
import 'dart:io';

import 'package:qr_code_scanner/qr_code_scanner.dart';
import 'package:flutter/material.dart';

import 'package:tpe/screens/payment.dart';
import 'package:tpe/screens/payment_sending.dart';
import 'package:tpe/utils/snackbar.dart';

class QrCodeReaderScreen extends StatelessWidget {
  const QrCodeReaderScreen({super.key, required this.price});

  final String price;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'QR Code Reader',
      home: QrCodeReaderScreenWidget(price: price),
      theme: ThemeData(
        scaffoldBackgroundColor: const Color(0xFF03045F),
        primarySwatch: Colors.blue,
        fontFamily: "Montserrat",
      ),
    );
  }
}

class QrCodeReaderScreenWidget extends StatefulWidget {
  const QrCodeReaderScreenWidget({super.key, required this.price});

  final String price;

  @override
  State<QrCodeReaderScreenWidget> createState() =>
      QrCodeReaderScreenWidgetState();
}

class QrCodeReaderScreenWidgetState extends State<QrCodeReaderScreenWidget> {
  final GlobalKey qrKey = GlobalKey(debugLabel: 'QR');
  Barcode? result;
  QRViewController? controller;

  @override
  void reassemble() {
    super.reassemble();
    if (Platform.isAndroid) {
      controller!.pauseCamera();
    }
    controller!.resumeCamera();
  }

  @override
  Widget build(BuildContext context) {
    if (controller != null && mounted) {
      controller!.pauseCamera();
      controller!.resumeCamera();
    }
    return Scaffold(
      body: Column(
        children: <Widget>[
          Expanded(flex: 4, child: _buildQrView(context)),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          _onBackButtonPressed();
        },
        backgroundColor: Colors.white,
        child: IconButton(
            onPressed: _onBackButtonPressed,
            color: Colors.white,
            icon: Image.asset("assets/img/arrow-left.png")),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.startTop,
    );
  }

  Widget _buildQrView(BuildContext context) {
    var scanArea = (MediaQuery.of(context).size.width < 400 ||
            MediaQuery.of(context).size.height < 400)
        ? 200.0
        : 250.0;
    return QRView(
      key: qrKey,
      onQRViewCreated: _onQRViewCreated,
      overlay: QrScannerOverlayShape(
        borderColor: Colors.black,
        borderRadius: 10,
        borderLength: 60,
        borderWidth: 10,
        cutOutSize: scanArea,
        overlayColor: Colors.black.withOpacity(0.7),
      ),
    );
  }

  void _onQRViewCreated(QRViewController controller) {
    setState(() {
      this.controller = controller;
    });
    controller.scannedDataStream.listen((scanData) {
      onDataReaded(scanData);
      setState(() {
        result = scanData;
      });
    });
  }

  void _onBackButtonPressed() {
    dispose();
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => PaymentScreen(
          price: widget.price,
        ),
      ),
    );
  }

  String describeEnum(Object? e) {
    if (e == null) return 'null';
    final String description = e.toString();
    final int indexOfDot = description.indexOf('.');
    assert(indexOfDot != -1 && indexOfDot + 1 < description.length);
    return description.substring(indexOfDot + 1);
  }

  void onDataReaded(Barcode data) {
    showSnackBar(context, "Scan réussi", "success", 2);
    Future.delayed(const Duration(milliseconds: 2000), () {
      dispose();
      paymentSendingScreen(data.code.toString());
    });
  }

  void paymentSendingScreen(String data) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => PaymentSendingScreen(
            paymentData: data, price: widget.price, paymentMethod: "qr_code"),
      ),
    );
  }

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    Future.delayed(const Duration(milliseconds: 200), () {
      controller?.stopCamera();
      controller?.dispose();
    });
    super.dispose();
  }
}
