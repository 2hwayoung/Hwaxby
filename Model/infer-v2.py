# from google.colab import drive
# drive.mount('/content/drive')

import os
import sys
from pathlib import Path
sys.path.append("/mnt/c/Users/USER/git/Hwaxby/Model/g2pk/")
sys.path.append("/mnt/c/Users/USER/git/Hwaxby/Model/TTS/")
# %cd /content/drive/My Drive/Conference
# !git clone --depth 1 https://github.com/sce-tts/TTS.git -b sce-tts
# !git clone --depth 1 https://github.com/sce-tts/g2pK.git
# %cd /content/drive/My Drive/Conference/TTS
# !pip install -q --no-cache-dir -e .
# %cd /content/drive/My Drive/Conference/g2pK
# !pip install -q --no-cache-dir "konlpy" "jamo" "nltk" "python-mecab-ko"
# !pip install -q --no-cache-dir -e .

# %cd /content/drive/My Drive/Conference/g2pK
import g2pk
g2p = g2pk.G2p()

import re
from unicodedata import normalize
# import IPython

from TTS.utils.synthesizer import Synthesizer

def normalize_text(text):
    text = text.strip()

    for c in ",;:":
        text = text.replace(c, ".")
    text = remove_duplicated_punctuations(text)

    text = jamo_text(text)

    text = g2p.idioms(text)
    text = g2pk.english.convert_eng(text, g2p.cmu)
    text = g2pk.utils.annotate(text, g2p.mecab)
    text = g2pk.numerals.convert_num(text)
    text = re.sub("/[PJEB]", "", text)

    text = alphabet_text(text)

    # remove unreadable characters
    text = normalize("NFD", text)
    text = "".join(c for c in text if c in symbols)
    text = normalize("NFC", text)

    text = text.strip()
    if len(text) == 0:
        return ""

    # only single punctuation
    if text in '.!?':
        return punctuation_text(text)

    # append punctuation if there is no punctuation at the end of the text
    if text[-1] not in '.!?':
        text += '.'

    return text


def remove_duplicated_punctuations(text):
    text = re.sub(r"[.?!]+\?", "?", text)
    text = re.sub(r"[.?!]+!", "!", text)
    text = re.sub(r"[.?!]+\.", ".", text)
    return text


def split_text(text):
    text = remove_duplicated_punctuations(text)

    texts = []
    for subtext in re.findall(r'[^.!?\n]*[.!?\n]', text):
        texts.append(subtext.strip())

    return texts


def alphabet_text(text):
    text = re.sub(r"(a|A)", "??????", text)
    text = re.sub(r"(b|B)", "???", text)
    text = re.sub(r"(c|C)", "???", text)
    text = re.sub(r"(d|D)", "???", text)
    text = re.sub(r"(e|E)", "???", text)
    text = re.sub(r"(f|F)", "??????", text)
    text = re.sub(r"(g|G)", "???", text)
    text = re.sub(r"(h|H)", "?????????", text)
    text = re.sub(r"(i|I)", "??????", text)
    text = re.sub(r"(j|J)", "??????", text)
    text = re.sub(r"(k|K)", "??????", text)
    text = re.sub(r"(l|L)", "???", text)
    text = re.sub(r"(m|M)", "???", text)
    text = re.sub(r"(n|N)", "???", text)
    text = re.sub(r"(o|O)", "???", text)
    text = re.sub(r"(p|P)", "???", text)
    text = re.sub(r"(q|Q)", "???", text)
    text = re.sub(r"(r|R)", "???", text)
    text = re.sub(r"(s|S)", "??????", text)
    text = re.sub(r"(t|T)", "???", text)
    text = re.sub(r"(u|U)", "???", text)
    text = re.sub(r"(v|V)", "??????", text)
    text = re.sub(r"(w|W)", "?????????", text)
    text = re.sub(r"(x|X)", "??????", text)
    text = re.sub(r"(y|Y)", "??????", text)
    text = re.sub(r"(z|Z)", "???", text)
    text = re.sub(r"0", "???", text)
    text = re.sub(r"1", "???", text)
    text = re.sub(r"2", "???", text)
    text = re.sub(r"3", "???", text)
    text = re.sub(r"4", "???", text)
    text = re.sub(r"5", "???", text)
    text = re.sub(r"6", "???", text)
    text = re.sub(r"7", "???", text)
    text = re.sub(r"8", "???", text)
    text = re.sub(r"9", "???", text)

    return text


def punctuation_text(text):
    # ????????????
    text = re.sub(r"!", "?????????", text)
    text = re.sub(r"\?", "?????????", text)
    text = re.sub(r"\.", "?????????", text)

    return text


def jamo_text(text):
    # ?????? ?????????
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "??????", text)
    text = re.sub(r"???", "?????????", text)
    text = re.sub(r"???", "?????????", text)
    text = re.sub(r"???", "?????????", text)
    text = re.sub(r"???", "?????????", text)
    text = re.sub(r"???", "?????????", text)
    text = re.sub(r"???", "????????????", text)
    text = re.sub(r"???", "????????????", text)
    text = re.sub(r"???", "????????????", text)
    text = re.sub(r"???", "????????????", text)
    text = re.sub(r"???", "????????????", text)
    text = re.sub(r"???", "????????????", text)
    text = re.sub(r"???", "????????????", text)
    text = re.sub(r"???", "????????????", text)
    text = re.sub(r"???", "????????????", text)
    text = re.sub(r"???", "????????????", text)
    text = re.sub(r"???", "????????????", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)
    text = re.sub(r"???", "???", text)

    return text


def normalize_multiline_text(long_text):
    texts = split_text(long_text)
    normalized_texts = [normalize_text(text).strip() for text in texts]
    return [text for text in normalized_texts if len(text) > 0]

def synthesize(text):
    wavs = synthesizer.tts(text, None, None)
    return wavs

synthesizer = Synthesizer(
    "/mnt/c/Users/USER/git/Hwaxby/Model/glowtts-v2/checkpoint_37000.pth.tar",
    "/mnt/c/Users/USER/git/Hwaxby/Model/glowtts-v2/config.json",
    None,
    "/mnt/c/Users/USER/git/Hwaxby/Model/hifigan-v2/checkpoint_295000.pth.tar",
    "/mnt/c/Users/USER/git/Hwaxby/Model/hifigan-v2/config.json",
    None,
    None,
    False,
)
symbols = synthesizer.tts_config.characters.characters

from scipy.io import wavfile
import numpy as np
from base64 import b64encode

texts = str(sys.argv[1])
# texts = """??? ???????????? ????????? 
# ?????? ????????????
# """

print(texts)

samplerate = 22050; fs = 100
t = np.linspace(0., 1., samplerate) 
amplitude = np.iinfo(np.int16).max
data = amplitude * np.sin(2. * np.pi * fs * t)
print("Data")
print(data)
print(type(data))
import json
import librosa
import soundfile as sf
result = ""

# for text in normalize_multiline_text(texts):
# print(text)
wav = synthesizer.tts(texts, None, None)
# wavjson = json.dumps(wav)
# basewav = b64encode(wavjson)
# print(basewav)
print(type(wav))
# print(wav)
sr = 22050
# wav, sr = librosa.load(librosa.util.example_audio_file(),duration=5.0)
# librosa.output.write_wav('/mnt/c/Users/USER/git/Hwaxby/Model/file_trim_5s.wav', wav, sr)
sf.write('/mnt/c/Users/USER/git/Hwaxby/Model/output.wav', wav, sr, format='WAV', endian='LITTLE', subtype='PCM_16')
# wavfile.write("/mnt/c/Users/USER/git/Hwaxby/Model/output", samplerate, data.astype(np.int16))
# wavfile.write("/mnt/c/Users/USER/git/Hwaxby/Model/output", samplerate, wav.astype(np.int16))
# wav.export("/mnt/c/Users/USER/git/Hwaxby/Model/output", format="wav")
# IPython.display.display(IPython.display.Audio(wav, rate=22050)) 
# with open("/content/drive/My Drive/Conference/final_output", "wb") as f:
#   f.write(bytes(wav))

with open("/mnt/c/Users/USER/git/Hwaxby/Model/output.wav",mode="rb") as f:
    # print(f)
    enc=b64encode(f.read())
#   print(enc)

with open("/mnt/c/Users/USER/git/Hwaxby/Model/output", mode="wb") as bf:
    bf.write(enc)
