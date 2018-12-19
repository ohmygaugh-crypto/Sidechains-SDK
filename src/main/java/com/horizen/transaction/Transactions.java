import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import scala.util.Try;
import scorex.core.serialization.BytesSerializable;
import scorex.core.serialization.Serializer;


/**
 *
 * IMPORTANT: Scorex Core "core.scala' file different from TreasuryCrypto 'core.scala'
 * TreasuryCrypto has older version of Scorex Core (before Catena changes on July 2018)
 *
 */

abstract class Transaction extends scorex.core.transaction.Transaction
{
    @Override
    public final scorex.core.ModifierTypeId modifierTypeId() {
        return (scorex.core.ModifierTypeId)super.modifierTypeId();
    }

    @Override
    public final scorex.core.ModifierId id() {
        return (scorex.core.ModifierId)super.id();
    }

    @Override
    public final byte[] bytes() {
        return serializer().toBytes(this);
    }

    @Override
    public abstract byte[] messageToSign();

    @Override
    public abstract TransactionSerializer serializer();

    public abstract scorex.core.ModifierTypeId transactionTypeId();
}


interface TransactionSerializer<T extends Transaction> extends Serializer<T>
{
    @Override
    byte[] toBytes(T obj);

    @Override
    Try<T> parseBytes(byte[] bytes);
}

// TO DO: do we need to put fee and timestamps members inside this abstract class?
abstract class BoxTransaction<P extends Proposition, B extends Box<P> > extends Transaction
{
    // TO DO: think about proper collection type
    public abstract ArrayList<BoxUnlocker<P>> unlockers();

    public abstract ArrayList<B> newBoxes();

    public abstract long fee();

    public abstract long timestamp();

    @Override
    public byte[] messageToSign() {
        // TO DO: return concatenation of newBoxes()[i].bytes() + unlockers()[i].closedBoxId() + timestamp + fee
        return new byte[0];
    }

    public TransactionIncompatibilityChecker incompatibilityChecker() {
        return new DefaultTransactionIncompatibilityChecker();
    }
}


interface TransactionIncompatibilityChecker<T extends BoxTransaction>
{
    boolean hasIncompatibleTransactions(T newTx, ArrayList<BoxTransaction> currentTxs);
}

class DefaultTransactionIncompatibilityChecker implements TransactionIncompatibilityChecker<BoxTransaction>
{
    @Override
    public boolean hasIncompatibleTransactions(BoxTransaction newTx, ArrayList<BoxTransaction> currentTxs) {
        // check intersections between spending boxes of current and txs
        return false;
    }
}

abstract class NoncedBoxTransaction<P extends Proposition, B extends NoncedBox<P> > extends BoxTransaction<P, B>
{

}


final class RegularTransaction extends NoncedBoxTransaction<PublicKey25519Proposition, RegularBox>
{

    @Override
    public RegularTransactionSerializer serializer() {
        return new RegularTransactionSerializer();
    }

    @Override
    public ArrayList<BoxUnlocker<PublicKey25519Proposition>> unlockers() {
        return null;
    }

    @Override
    public ArrayList<RegularBox> newBoxes() {
        return null;
    }

    @Override
    public long fee() {
        return 0;
    }

    @Override
    public long timestamp() {
        return 0;
    }

    @Override
    public scorex.core.ModifierTypeId transactionTypeId() {
        return null;// scorex.core.ModifierTypeId @@ 1.toByte();
    }
}

class RegularTransactionSerializer implements TransactionSerializer<RegularTransaction>
{
    private ListSerializer<RegularBox> _boxSerializer;
    // todo: keep another serializers for inputs and signatures(secrets)

    RegularTransactionSerializer() {
        HashMap<Integer, Serializer<RegularBox>> supportedBoxSerializers = new HashMap<Integer, Serializer<RegularBox>>();
        supportedBoxSerializers.put(1, new RegularBoxSerializer());

        _boxSerializer  = new ListSerializer<RegularBox>(supportedBoxSerializers);
    }

    @Override
    public byte[] toBytes(RegularTransaction obj) {
        return _boxSerializer.toBytes(obj.newBoxes());
    }

    @Override
    public Try<RegularTransaction> parseBytes(byte[] bytes) {
        ArrayList<RegularBox> boxes = _boxSerializer.parseBytes(bytes).get();

        // create RegualrTransaction and init with Boxes
        return null;
    }
}


final class MC2SCAggregatedTransaction extends BoxTransaction<Proposition, Box<Proposition>>
{

    @Override
    public MC2SCAggregatedTransactionSerializer serializer() {
        return new MC2SCAggregatedTransactionSerializer();
    }

    @Override
    public ArrayList<BoxUnlocker<Proposition>> unlockers() {
        // create array and put BoxUnlocker<ProofOfCoinBurnProposition> and/or BoxUnlocker<ProofOfBeingIncludedIntoCertificateProposition> inside
        return null;
    }

    @Override
    public ArrayList<Box<Proposition>> newBoxes() {
        // return list of RegularBoxes for MC2SC coins and CertifierRightBoxes for Certifier locks
        return null;
    }

    @Override
    public long fee() {
        return 0;
    }

    @Override
    public long timestamp() {
        return 0;
    }

    @Override
    public scorex.core.ModifierTypeId transactionTypeId() {
        return null; // scorex.core.ModifierTypeId @@ 2.toByte
    }
}

class MC2SCAggregatedTransactionSerializer implements TransactionSerializer<MC2SCAggregatedTransaction>
{
    private ListSerializer<Box<Proposition>> _boxSerializer;

    MC2SCAggregatedTransactionSerializer() {
        HashMap<Integer, Serializer<Box>> supportedBoxSerializers = new HashMap<Integer, Serializer<Box>>();
        //supportedBoxSerializers.put(1, new RegularBoxSerializer());
        //_boxSerializer  = new ListSerializer<Box<Proposition>>(supportedBoxSerializers);
    }

    @Override
    public byte[] toBytes(MC2SCAggregatedTransaction obj) {
        return _boxSerializer.toBytes(obj.newBoxes());
    }

    @Override
    public Try<MC2SCAggregatedTransaction> parseBytes(byte[] bytes) {
        ArrayList<Box<Proposition>> boxes = _boxSerializer.parseBytes(bytes).get();

        // create RegualrTransaction and init with Boxes
        return null;
    }
}


final class WithdrawalRequestTransaction extends NoncedBoxTransaction<Proposition, NoncedBox<Proposition>>
{
    @Override
    public WithdrawalRequestTransactionSerializer serializer() {
        return new WithdrawalRequestTransactionSerializer();
    }

    @Override
    public ArrayList<BoxUnlocker<Proposition>> unlockers() { return null; }

    // nothing to create
    @Override
    public ArrayList<NoncedBox<Proposition>> newBoxes() {
        return new ArrayList<NoncedBox<Proposition>>();
    }

    @Override
    public long fee() {
        return 0;
    }

    @Override
    public long timestamp() {
        return 0;
    }

    @Override
    public scorex.core.ModifierTypeId transactionTypeId() {
        return null; // scorex.core.ModifierTypeId @@ 3.toByte
    }
}


class WithdrawalRequestTransactionSerializer implements TransactionSerializer<WithdrawalRequestTransaction>
{
    private ListSerializer<NoncedBox<Proposition>> _boxSerializer;

    WithdrawalRequestTransactionSerializer() {
        HashMap<Integer, Serializer<NoncedBox<Proposition>>> supportedBoxSerializers = new HashMap<Integer, Serializer<NoncedBox<Proposition>>>();
        //supportedBoxSerializers.put(1, new RegularBoxSerializer());
        // TO DO: update supported serializers list

        _boxSerializer  = new ListSerializer<NoncedBox<Proposition>>(supportedBoxSerializers);
    }

    @Override
    public byte[] toBytes(WithdrawalRequestTransaction obj) {
        return _boxSerializer.toBytes(obj.newBoxes());
    }

    @Override
    public Try<WithdrawalRequestTransaction> parseBytes(byte[] bytes) {
        ArrayList<NoncedBox<Proposition>> boxes = _boxSerializer.parseBytes(bytes).get();
        return null;
    }
}


final class CertifierUnlockRequestTransaction extends NoncedBoxTransaction<Proposition, NoncedBox<Proposition>>
{
    @Override
    public CertifierUnlockRequestTransactionSerializer serializer() {
        return new CertifierUnlockRequestTransactionSerializer();
    }

    @Override
    public ArrayList<BoxUnlocker<Proposition>> unlockers() { return null; }

    // nothing to create
    @Override
    public ArrayList<NoncedBox<Proposition>> newBoxes() {
        return new ArrayList<NoncedBox<Proposition>>();
    }

    @Override
    public long fee() {
        return 0;
    }

    @Override
    public long timestamp() {
        return 0;
    }

    @Override
    public scorex.core.ModifierTypeId transactionTypeId() {
        return null; // scorex.core.ModifierTypeId @@ 3.toByte
    }
}


class CertifierUnlockRequestTransactionSerializer implements TransactionSerializer<CertifierUnlockRequestTransaction>
{
    private ListSerializer<NoncedBox<Proposition>> _boxSerializer;

    CertifierUnlockRequestTransactionSerializer() {
        HashMap<Integer, Serializer<NoncedBox<Proposition>>> supportedBoxSerializers = new HashMap<Integer, Serializer<NoncedBox<Proposition>>>();
        //supportedBoxSerializers.put(1, new RegularBoxSerializer());
        // TO DO: update supported serializers list

        _boxSerializer  = new ListSerializer<NoncedBox<Proposition>>(supportedBoxSerializers);
    }

    @Override
    public byte[] toBytes(CertifierUnlockRequestTransaction obj) {
        return _boxSerializer.toBytes(obj.newBoxes());
    }

    @Override
    public Try<CertifierUnlockRequestTransaction> parseBytes(byte[] bytes) {
        ArrayList<NoncedBox<Proposition>> boxes = _boxSerializer.parseBytes(bytes).get();
        return null;
    }
}



class ListSerializer<T extends BytesSerializable> implements Serializer<ArrayList<T>> {
    private HashMap<Integer, Serializer<T>> _serializers; // unique key + serializer

    ListSerializer(HashMap<Integer, Serializer<T>> serializers) {
        _serializers = serializers;
    }

    @Override
    public byte[] toBytes(ArrayList<T> obj) {
        ArrayList<Integer> lengthList = new ArrayList<Integer>();

        ByteArrayOutputStream res = new ByteArrayOutputStream();
        ByteArrayOutputStream entireRes = new ByteArrayOutputStream();
        for (T t : obj) {
            Integer idOfSerializer = 0;// get id from _serializers
            byte[] tBytes = t.bytes();
            lengthList.add(idOfSerializer.byteValue() + tBytes.length);

            try {
                entireRes.write(idOfSerializer);
                entireRes.write(t.bytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            res.write(lengthList.size());
            for (Integer i : lengthList) {
                res.write(i);
            }
            res.write(entireRes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res.toByteArray();
    }

    @Override
    public Try<ArrayList<T>> parseBytes(byte[] bytes) {
        // TO DO: implement backward logic
        return null;
    }
}