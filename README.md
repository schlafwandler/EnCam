# EnCam - an encrypted Camera application for android

## What is EnCam?

EnCam stands for ENcrypted CAMera. The EnCam project aims at creating a Camera App for Android 
that stores pictures in a (more) secure way. To achieve this goal, the pictures are encrypted 
using APG, a free OpenPGP implementation for Android, before they are written to the storage medium.

## How it works

EnCam can be uses quite similar to the standard android camera app. But when a picture is taken, 
the JPEG data is passed to the Android Privacy Guard, an GPG implementation for andoid. APG then encrypts 
the pictures data using one (or more) public GPG keys.

After this, the data can only be read (decrypted) using the private key.

Only the public must reside on the phone; the private key can (and should) be stored completely separated. 
It is only needed to decrypt (watch) the encypted pictures. Depending on your security needs, you can 
store the private key on an encrypedt partition on your home computer, an USB drive burried in the woods
or with a trusted person in an other (safer) country.

At no time it is nessesaray to enter a passphrase or something similar on the device.     

See the Wikipedia articles about [Asymmetric_cryptography](http://en.wikipedia.org/wiki/Asymmetric_cryptography) 
or [GPG](http://en.wikipedia.org/wiki/GNU_Privacy_Guard) for an overview on asymmetric cryptography and GPG.

## Why encrypt pothgraphs?

Einsatzszenario ist zum Beispiel das Dokumentieren von Polizeigewalt
o.Ã¤. auf einer Demonstration, ohne das die Gefahr besteht, das die
Bilder in die HÃ¤nde der Staatsgewalt fallen und gegen Demonstranten
verwendet werden.

## Installation

Make sure that [APG](http://code.google.com/p/android-privacy-guard/) is installed properpy and import one of 
your public GPG keys to APG. See [the APG homepage](http://thialfihar.org/projects/apg/) for details.
You migth want to create a dedicated public/private keypair just for the encyption of pictures.  

1. Download the latest release of EnCam


2. public keys auswÃ¤hlen:
Menu -> Select public keys -> HÃ¤kchen machen

3. Fotos machen:
Bilder werden mit dem Directional Pad Center key (KEYCODE_DPAD_CENTER)
gemacht (gleiche Taste wie bei der standard android Camera). Die Bilder
werden verschlÃ¼sselt auf der sdCard im Ordner 'Encrypted Pictures'
gespeichert. Der Name ist zufallsgeneriert, die Endung immer '.ejpg'

4. Fotos entschlÃ¼sseln:
Die verschlÃ¼sselten Bilder aus dem Ordner 'Encrypted Pictures' kÃ¶nnen
auf einen anderen (am besten verschlÃ¼sselten) DatentrÃ¤ger kopiert und
dort mit gpg entschlÃ¼sselt werden.


## Potential weak spots

The usual: the private key must remain secret
By now, I can not rule out that an uncenrypted copy of the data remains in 
the phones RAM after the encyption process. It *might* be possible to read
out the phones RAM and reconstruct the original picture.

## Future development

* I plan to add a feature that allows to transmitt the encrypted pictures via e-mail 
directly after they are encypted. Thereby existes a backup of the pictures even if the phone is lost/destroyed/seized.
* a support for videos is planed