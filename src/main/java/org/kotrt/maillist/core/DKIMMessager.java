package org.kotrt.maillist.core;

import org.kotrt.maillist.core.context.Context;

import javax.mail.Message;
import javax.mail.Session;

/**
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-28.
 */
public class DKIMMessager extends Messager {

    public DKIMMessager(MailProperty property, Session session) {
        super(property, session);
    }

    @Override
    protected Message newMessage(Session session) {
        final MailProperty props = Context.getInstance().getMailProperty();

        try {
//            //Create DKIM Signer
//            DKIMSigner dkimSigner = props.newDKIMSigner();
//            dkimSigner.setIdentity(props.getUsername() + "@" + props.getDKIMSigningdomain());
//            dkimSigner.setHeaderCanonicalization(Canonicalization.SIMPLE);
//            dkimSigner.setBodyCanonicalization(Canonicalization.RELAXED);
//            dkimSigner.setLengthParam(true);
//            dkimSigner.setSigningAlgorithm(SigningAlgorithm.SHA1withRSA);
//            dkimSigner.setZParam(true);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
