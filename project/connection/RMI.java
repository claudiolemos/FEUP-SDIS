package connection;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * remote class implemented by peer
 */
public interface RMI extends Remote {
    void backup(String filepath, int replicationDegree) throws RemoteException;
    void restore(String filepath) throws RemoteException;
    void delete(String filepath) throws RemoteException;
    void reclaim(int reclaimSpace) throws RemoteException;
    void state() throws RemoteException;
}
