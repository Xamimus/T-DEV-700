import 'dart:math';
import 'package:flutter/material.dart';

import 'package:tpe/screens/payment_success.dart';
import 'package:tpe/screens/payment_error.dart';
import 'package:tpe/screens/nfc_reader.dart';
import 'package:tpe/screens/qr_code_reader.dart';

class PaymentScreen extends StatelessWidget {
  const PaymentScreen({super.key, required this.price});

  final String price;
  static const String _title = 'Payment method';

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: _title,
      home: PaymentScreenStatefulWidget(
        price: price,
      ),
      theme: ThemeData(
          scaffoldBackgroundColor: const Color(0xFF03045F),
          primarySwatch: Colors.blue,
          fontFamily: "Montserrat"),
    );
  }
}

class PaymentScreenStatefulWidget extends StatefulWidget {
  const PaymentScreenStatefulWidget({super.key, required this.price});

  final String price;

  @override
  State<PaymentScreenStatefulWidget> createState() =>
      _PaymentScreenStatefulWidgetState();
}

class _PaymentScreenStatefulWidgetState
    extends State<PaymentScreenStatefulWidget> {
  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  void _onPaymentSent() {
    dispose();
    Random random = Random();
    StatelessWidget screen = random.nextBool()
        ? PaymentSuccessScreen(
            price: widget.price,
          )
        : const PaymentErrorScreen();
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => screen,
      ),
    );
  }

  void _onNfcSelected() {
    /* Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => NfcReaderScreen(price: widget.price),
      ),
    ); */
    Navigator.pop(context);
  }

  void _onQrCodeSelected() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => QrCodeReaderScreen(price: widget.price),
      ),
    );
  }

  /* @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Text(
                  "Montant: ${widget.price}",
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    fontWeight: FontWeight.w700,
                    color: Colors.white,
                    fontSize: 32,
                    letterSpacing: 0.02,
                    height: 1.2,
                  ),
                ),
              ],
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Container(
                  margin: const EdgeInsets.only(bottom: 20),
                  child: IconButton(
                    icon: Image.asset('assets/img/qr_code.png'),
                    iconSize: 300,
                    onPressed: () {
                      _onQrCodeSelected();
                    },
                  ),
                )
              ],
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Container(
                  transform: Matrix4.translationValues(0, -85, 0),
                  child: const Text(
                    "Payer par chèque",
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontWeight: FontWeight.w700,
                      color: Colors.white,
                      fontSize: 25,
                      letterSpacing: 0.02,
                      height: 1.2,
                    ),
                  ),
                )
              ],
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                IconButton(
                  icon: Image.asset('assets/img/nfc.png'),
                  iconSize: 300,
                  onPressed: () {
                    _onNfcSelected();
                  },
                )
              ],
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Container(
                  transform: Matrix4.translationValues(0, -60, 0),
                  child: const Text(
                    "Payer par NFC",
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontWeight: FontWeight.w700,
                      color: Colors.white,
                      fontSize: 25,
                      letterSpacing: 0.02,
                      height: 1.2,
                    ),
                  ),
                )
              ],
            ),
          ],
        ),
      ),
    );
  } */

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        body: Padding(
      padding: const EdgeInsets.all(20.0),
      child: Padding(
        padding: const EdgeInsets.only(top: 35),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: <Widget>[
              Text(
                "Montant: ${widget.price}",
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  color: Colors.white,
                  fontSize: 32,
                  letterSpacing: 0.02,
                  height: 1.2,
                ),
              ),
              Stack(
                clipBehavior: Clip.none,
                alignment: Alignment.center,
                children: <Widget>[
                  Container(
                    margin: const EdgeInsets.only(bottom: 20),
                    child: IconButton(
                      icon: Image.asset('assets/img/qr_code.png'),
                      iconSize: 300,
                      onPressed: () {
                        _onQrCodeSelected();
                      },
                    ),
                  ),
                  const Positioned(
                    bottom: 45,
                    child: Text(
                      "Payer par chèque",
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontWeight: FontWeight.w700,
                        color: Colors.white,
                        fontSize: 25,
                        letterSpacing: 0.02,
                        height: 1.2,
                      ),
                    ),
                  ),
                ],
              ),
              Stack(
                  clipBehavior: Clip.none,
                  alignment: Alignment.center,
                  children: <Widget>[
                    Container(
                      margin: const EdgeInsets.only(bottom: 20),
                      child: IconButton(
                        icon: Image.asset('assets/img/nfc.png'),
                        iconSize: 300,
                        onPressed: () {
                          _onNfcSelected();
                        },
                      ),
                    ),
                    const Positioned(
                      bottom: 45,
                      child: Text(
                        "Payer par NFC",
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontWeight: FontWeight.w700,
                          color: Colors.white,
                          fontSize: 25,
                          letterSpacing: 0.02,
                          height: 1.2,
                        ),
                      ),
                    ),
                  ]),
              /* IconButton(
                icon: Image.asset('assets/img/nfc.png'),
                iconSize: 300,
                onPressed: () {
                  _onNfcSelected();
                },
              ),
              Container(
                transform: Matrix4.translationValues(0, -60, 0),
                child: const Text(
                  "Payer par NFC",
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontWeight: FontWeight.w700,
                    color: Colors.white,
                    fontSize: 25,
                    letterSpacing: 0.02,
                    height: 1.2,
                  ),
                ),
              ) */
            ],
          ),
        ),
      ),
    ));
  }
}
