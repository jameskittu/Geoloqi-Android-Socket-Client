/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/aaronpk/Documents/eclipse-workspace/Geoloqi-Android-Socket-Client/src/com/geoloqi/socket/IGeoloqiService.aidl
 */
package com.geoloqi.socket;
public interface IGeoloqiService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.geoloqi.socket.IGeoloqiService
{
private static final java.lang.String DESCRIPTOR = "com.geoloqi.socket.IGeoloqiService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.geoloqi.socket.IGeoloqiService interface,
 * generating a proxy if needed.
 */
public static com.geoloqi.socket.IGeoloqiService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.geoloqi.socket.IGeoloqiService))) {
return ((com.geoloqi.socket.IGeoloqiService)iin);
}
return new com.geoloqi.socket.IGeoloqiService.Stub.Proxy(obj);
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
case TRANSACTION_getPid:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getPid();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_lastLocationDate:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.lastLocationDate();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.geoloqi.socket.IGeoloqiService
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
public int getPid() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPid, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int lastLocationDate() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_lastLocationDate, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getPid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_lastLocationDate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public int getPid() throws android.os.RemoteException;
public int lastLocationDate() throws android.os.RemoteException;
}
