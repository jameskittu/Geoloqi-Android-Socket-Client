/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/aaronpk/Documents/eclipse-workspace/Geoloqi-Android-Socket-Client/src/com/geoloqi/socket/IGeoloqiServiceCallback.aidl
 */
package com.geoloqi.socket;
/**
 * Example of a callback interface used by IRemoteService to send
 * synchronous notifications back to its clients.  Note that this is a
 * one-way interface so the server does not block waiting for the client.
 */
public interface IGeoloqiServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.geoloqi.socket.IGeoloqiServiceCallback
{
private static final java.lang.String DESCRIPTOR = "com.geoloqi.socket.IGeoloqiServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.geoloqi.socket.IGeoloqiServiceCallback interface,
 * generating a proxy if needed.
 */
public static com.geoloqi.socket.IGeoloqiServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.geoloqi.socket.IGeoloqiServiceCallback))) {
return ((com.geoloqi.socket.IGeoloqiServiceCallback)iin);
}
return new com.geoloqi.socket.IGeoloqiServiceCallback.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_locationUpdated:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.locationUpdated(_arg0);
return true;
}
case TRANSACTION_messageReceived:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.messageReceived(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.geoloqi.socket.IGeoloqiServiceCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Called when the service has a new value for you.
     */
public void locationUpdated(int value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(value);
mRemote.transact(Stub.TRANSACTION_locationUpdated, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
public void messageReceived(int value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(value);
mRemote.transact(Stub.TRANSACTION_messageReceived, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_locationUpdated = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_messageReceived = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Called when the service has a new value for you.
     */
public void locationUpdated(int value) throws android.os.RemoteException;
public void messageReceived(int value) throws android.os.RemoteException;
}
