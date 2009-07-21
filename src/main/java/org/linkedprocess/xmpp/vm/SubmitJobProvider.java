package org.linkedprocess.xmpp.vm;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.linkedprocess.LinkedProcess;

/**
 * User: marko
 * Date: Jun 24, 2009
 * Time: 11:29:58 AM
 */
public class SubmitJobProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        SubmitJob submitJob = new SubmitJob();

        String vmPassword = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.VM_PASSWORD_ATTRIBUTE);
        if(null != vmPassword) {
            submitJob.setVmPassword(vmPassword);
        }

        String errorType = parser.getAttributeValue(LinkedProcess.BLANK_NAMESPACE, LinkedProcess.ERROR_TYPE_ATTRIBUTE);
        if(null != errorType) {
            submitJob.setErrorType(LinkedProcess.ErrorType.getErrorType(errorType));
        }

        int v = parser.next();
        if(v == XmlPullParser.TEXT) {
            String textBody =  parser.getText();
            if(textBody != null) {
                if(null != submitJob.getErrorType())
                    submitJob.setErrorMessage(textBody);
                else
                    submitJob.setExpression(textBody);
            }
            parser.next();
        }
        return submitJob;
    }
}
