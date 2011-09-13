# EnCam - an encrypted Camera application for android

## What is EnCam?

EnCam stands for ENcrypted CAMera. The EnCam project aims at creating a Camera App for Android 
that stores pictures in a (more) secure way. To achieve this goal, the pictures are encrypted 
using APG, a free OpenPGP implementation for Android, before they are written to the storage medium.

## Status

EnCam is currently a alpha version. The basic features are working, but I wouldn't recommend using it in it's current
state - at least not in situations where there is something at stake. 

## How it works

EnCam can be uses quite similar to the standard android camera app. But when a picture is taken, 
the JPEG data is passed to the Android Privacy Guard, an GPG implementation for andoid. APG then encrypts 
the pictures data using one (or more) public GPG keys.

After this, the data can only be read (decrypted) using the private key.

Only the public key must reside on the phone; the private key can (and should) be stored completely separated. 
It is only needed to decrypt (watch) the encrypted pictures. Depending on your security needs, you can 
store the private key on an encrypted partition on your home computer, an USB drive buried in the woods
or with a trusted person in an other (safer) country.

At no time it is necessary to enter a passphrase or something similar on the device.     

See the Wikipedia articles about [Asymmetric_cryptography](http://en.wikipedia.org/wiki/Asymmetric_cryptography) 
or [GPG](http://en.wikipedia.org/wiki/GNU_Privacy_Guard) for an overview on asymmetric cryptography and GPG.

## Why encrypt photographs?

The need to take pictures without risking these pictures falling into the wrong hands can arise in many 
different situations, especially during political work.
For example, it is a common problem during political protest that on one hand pictures are needed for documentation, evidence 
or to bring events to public attention, while on the other hand these pictures can fall into the hands of 
rogue police forces or worse, putting political activists in danger.

EnCam is intended as a tool to lower the risk of taking pictures in such situations.

**EnCam is not, and never will be, a complete solution to all the problems that can arise if you take
picture that can put others or yourself in danger. It is intended as a tool that can, if used in the right
 way and in conjunction with other security measure, can take away a portion of the risk. Nothing more, nothing less.**


## Usage

Make sure that [APG](http://code.google.com/p/android-privacy-guard/) is installed properly and import one of 
your public GPG keys to APG. See [the APG homepage](http://thialfihar.org/projects/apg/) for details.
You might want to create a dedicated public/private keypair just for the encryption of pictures.  

### Install the latest apk file on your phone
You can either use a file manager, an special installer app (various are available in the market) or the 
android SDK to install the apk.

#### Installation using the SDK
`adb install EnCam.apk`

### Select public keys:
Go 
`Menu -> Select public keys`
 and check the keys you want your pictures to be encrypted with.

### Take pictures
You can take pictures using either the button on screen or the Directional Pad Center key (if
 your phone has one). The pictures are encrypted on the fly and saved in the folder 'Encrypted Pictures'
 on the storage medium. The name is generated randomly, the suffix is always '.ejpg'.

### Decrypt photos
The encrypted photographs are stored in the folder 'Encrypted Pictures' on the SD-Card. 
They can be copied to any computer and decrypted using gpg. For example,  if you want to decrypt
the file **226f9415.ejpg**, the command is
`gpg --output 226f9415.jpg --decrypt 226f9415.ejpg`

You should store the decrypted pictures only on secured media, like an encrypted drive. 


## Potential weak spots

The usual: the private key must remain secret.

By now, I can not rule out that an unencrypted copy of the data remains in 
the phones RAM after the encryption process. It *might* be possible to read
out the phones RAM and reconstruct the original picture.

## Known bugs

* there is a problem with some custom ROMs. EnCam starts normally, but no pictures can be taken. Cause is unknown by now.

## Future development

* implement zoom
* add a script to automatically copy and decrypt pictures
* I plan to add a feature that allows to transmit the encrypted pictures via e-mail 
directly after they are encrypted. Thereby a backup of the pictures exists even if the phone is lost/destroyed/seized.
* a support for videos is planed, but won't be ready soon