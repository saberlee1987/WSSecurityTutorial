package com.example.tutorial.ws.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

/**
 * Really bad service keystore password callback.
 * 
 * @author Ross M. Lodge
 */
public class KeystorePasswordCallback implements CallbackHandler
{

    private Map<String, String> passwords = new HashMap<String, String>();

    /**
     * {@inheritDoc}
     * 
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
        for (Callback callback : callbacks)
        {
            if (callback instanceof WSPasswordCallback)
            {
                WSPasswordCallback pc = (WSPasswordCallback)callback;
    
                String pass = passwords.get(pc.getIdentifier());
                if (pass != null)
                {
                    pc.setPassword(pass);
                    return;
                }
            }
        }
    }

    /**
     * @return the passwords
     */
    public Map<String, String> getPasswords()
    {
        return passwords;
    }

    /**
     * @param passwords the passwords to set
     */
    public void setPasswords(Map<String, String> passwords)
    {
        this.passwords = passwords;
    }
    
}
