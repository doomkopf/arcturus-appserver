# The predecessor of the gammaray-appserver

This is the predecessor of the gammaray-appserver written in Java and is obviously not maintained anymore.
It was able to handle apps written in Java and Node.js using first the Nashorn engine and later a native V8 engine bridge.

The idea about what to achieve with it changed many times and it got too complex. And Java can become very complicated when it comes to threading, contexts and async I/O. The idea of writing a backend app in JS was the actual goal so why not write the whole appserver in JS? -> gammaray-appserver

But many components might still be reusable for other Java based projects, so for documentation reasons the repo will stay here. E.g. the distributed transaction system, which the gammaray-appserver doesn't support yet.