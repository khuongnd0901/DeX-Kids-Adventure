#!/usr/bin/env python3
"""T-070 original editable illustrations, extending the project's SVG prop system.
No network. This script is source authoring, NOT part of automatic asset rebuilds.
"""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'art/assets-source/story-world/objects'
def rect(x,y,w,h,c,rx=8):return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{c}"/>'
def circle(x,y,r,c):return f'<circle cx="{x}" cy="{y}" r="{r}" fill="{c}"/>'
def ellipse(x,y,rx,ry,c):return f'<ellipse cx="{x}" cy="{y}" rx="{rx}" ry="{ry}" fill="{c}"/>'
def path(d,c='none',stroke=None,width=5):return f'<path d="{d}" fill="{c}"'+(f' stroke="{stroke}" stroke-width="{width}" stroke-linecap="round" stroke-linejoin="round"' if stroke else '')+'/>'
def line(x,y,a,b,c='#526875',w=5):return path(f'M{x} {y}L{a} {b}',stroke=c,width=w)
def eyes(x=80,y=75,space=32):return circle(x,y,4,'#243746')+circle(x+space,y,4,'#243746')
def wheel(x,y):return circle(x,y,20,'#314a60')+circle(x,y,14,'#d1edf4')+circle(x,y,4,'#5d86a4')
def leaf(x,y):return ellipse(x,y,12,6,'#5baf68')
wood='#bb8252';blue='#55a8d9';red='#f2776d';yellow='#f6c65b';green='#7ebf87';cream='#fff2d3';ink='#34526a'
art={}
art['TABLE']=rect(24,69,144,15,wood)+rect(36,82,13,65,wood)+rect(143,82,13,65,wood)
art['CHAIR']=rect(51,23,91,64,yellow)+rect(43,86,108,17,wood)+rect(52,100,13,48,wood)+rect(130,100,13,48,wood)
art['BED']=rect(20,70,18,73,wood)+rect(35,83,137,41,cream)+rect(79,82,88,43,blue)+rect(40,72,41,21,'#fff9ec')+rect(158,95,15,48,wood)
art['PILLOW']=path('M34 53 Q96 32 158 53 Q143 92 159 123 Q96 139 33 122 Q48 88 34 53',cream,wood,3)+path('M45 64 Q96 50 145 64',stroke='#e8d3a7',width=3)
art['BLANKET']=path('M42 30 L150 42 L141 139 L30 126 Z',blue)+path('M55 32L45 128M85 36L75 132M116 39L107 136M35 69L147 81M32 104L144 116',stroke='#b5e4e8',width=8)
art['LAMP']=rect(84,63,13,69,wood)+ellipse(90,138,44,8,ink)+path('M58 28L124 28L151 83L31 83Z',yellow)+circle(90,86,8,cream)
art['CUP']=path('M135 59 C181 45 181 112 138 106',stroke=blue,width=13)+path('M41 42L140 42L131 128Q91 145 50 128Z',blue)+ellipse(91,42,49,10,'#b8e9ef')
art['BOWL']=path('M24 61L169 61Q160 141 97 142Q36 140 24 61',blue)+ellipse(97,61,72,17,'#dcf3e9')
art['SPOON']=ellipse(90,50,22,31,'#bed4e2')+rect(82,75,16,74,'#a1bbcd')+ellipse(84,43,7,15,'#eff7fc')
art['WATER_BOTTLE']=rect(74,15,44,18,blue)+rect(69,35,54,111,'#92d2de',16)+rect(69,76,54,36,'#faf6df')+circle(96,94,9,blue)
art['TOOTHBRUSH']=rect(86,51,17,96,blue)+rect(76,29,36,39,cream)+path('M81 20V39M89 17V39M98 17V39M107 20V39',stroke='#a0d3d7',width=5)
art['TOWEL']=rect(39,28,111,116,'#e7909b',4)+rect(39,109,111,10,cream,0)+path('M48 143V151M63 143V151M79 143V151M96 143V151M112 143V151M130 143V151M143 143V151',stroke='#c06e7d',width=3)
art['PENCIL']=rect(84,28,24,109,yellow,2)+path('M84 137L96 160L108 137Z',wood)+path('M91 151L96 160L101 151Z',ink)+rect(84,16,24,17,red,2)
art['ERASER']=path('M42 68L125 33L160 102L77 138L35 107Z',red)+path('M91 47L125 33L160 102L127 117Z',blue)+line(49,70,128,117,'#ffbbb0',3)
art['RULER']=path('M26 105L155 35L173 70L44 141Z',yellow)+''.join(line(39+i*14,102-i*7,45+i*14,114-i*7,wood,3) for i in range(9))
art['NOTEBOOK']=rect(44,25,111,126,blue)+rect(57,32,90,111,cream)+''.join(line(66,y,136,y,'#b6ced4',2) for y in range(58,131,18))+''.join(circle(49,y,4,ink) for y in range(42,140,20))
art['BACKPACK']=path('M71 38Q96 6 121 38',stroke=wood,width=10)+rect(43,37,107,112,blue,25)+rect(55,93,83,43,yellow,12)+path('M55 109H138',stroke=wood,width=3)+rect(32,75,13,53,wood)+rect(149,75,13,53,wood)
art['CRAYONS']=''.join(path(f'M{35+i*27} 65L{43+i*27} 35L{51+i*27} 65V141H{35+i*27}Z',c) for i,c in enumerate((red,yellow,green,blue,'#a280c7')))+rect(28,91,145,23,cream,2)
art['BANANA']=path('M42 38Q61 110 150 55Q148 115 83 133Q28 139 29 74Z',yellow)+path('M42 38Q42 88 98 103',stroke='#da9d35',width=4)+rect(29,28,13,20,wood)
art['ORANGE']=circle(96,94,53,'#f5a343')+circle(77,74,23,'#ffc26c')+line(96,42,100,28,wood,7)+leaf(113,29)
art['CARROT']=path('M65 49L134 63L78 151Z','#f29345')+path('M94 52Q50 13 74 17M101 50Q114 6 126 21M106 50Q153 21 150 42',stroke=green,width=10)+line(76,81,99,88,'#d97831',3)+line(81,111,91,114,'#d97831',3)
art['BREAD']=path('M40 67Q20 26 58 23Q95 5 131 23Q173 25 152 67L151 142L41 142Z','#c88e50')+path('M53 69Q40 42 62 38Q95 24 126 38Q151 40 139 68L137 127L55 127Z','#f6d49a')
art['RICE']=path('M27 85H167Q158 146 97 147Q34 141 27 85',blue)+ellipse(97,82,70,24,cream)+''.join(ellipse(50+(i%6)*18,65+(i//6)*12,7,3,'#fffdf2') for i in range(18))
art['MILK']=path('M56 50L78 22H120L141 50V149H56Z','#eef7f0')+path('M56 50H141V74H56Z',blue)+rect(63,92,72,41,blue)+path('M82 26V45H137',stroke='#adcdd9',width=3)+circle(100,111,12,'#e2f2f5')
art['TEDDY_BEAR']=circle(57,40,22,wood)+circle(131,40,22,wood)+ellipse(95,110,42,43,wood)+circle(95,64,44,'#c79662')+circle(75,63,4,ink)+circle(114,63,4,ink)+ellipse(95,80,20,14,cream)+circle(95,75,6,ink)+ellipse(49,109,17,28,wood)+ellipse(143,109,17,28,wood)+ellipse(64,144,23,13,wood)+ellipse(126,144,23,13,wood)
art['BLOCKS']=rect(28,97,45,45,red)+rect(77,97,45,45,blue)+rect(126,97,45,45,yellow)+rect(53,48,45,45,green)+rect(102,48,45,45,'#a88ad0')+path('M77 16L99 44H54Z',yellow)
art['PUZZLE']=rect(31,36,132,113,blue)+path('M31 91H77Q66 71 86 71Q106 72 95 91H163M96 36V68M96 112V149',stroke=cream,width=6)+circle(124,67,13,yellow)+path('M51 127L69 105L84 127Z',green)
art['DOLL']=circle(96,43,27,'#b27f5a')+circle(96,46,22,'#f4c7a0')+eyes(88,45,17)+path('M67 76L126 76L145 130H47Z',red)+line(72,133,66,150,wood,12)+line(117,133,124,150,wood,12)+line(66,82,45,111,'#f4c7a0',13)+line(125,82,150,112,'#f4c7a0',13)
art['TOY_TRAIN']=rect(33,71,84,55,red)+rect(111,43,47,84,blue)+rect(107,33,55,12,yellow)+rect(121,51,26,28,'#bde9ea')+rect(42,53,20,20,ink)+circle(53,133,16,ink)+circle(98,133,16,ink)+circle(144,133,16,ink)+rect(22,120,148,9,yellow)
art['BUBBLES']=''.join(circle(x,y,r,'#b5e7ed')+path(f'M{x-r*.5} {y-r*.3}Q{x-r*.3} {y-r*.7} {x+r*.1} {y-r*.6}',stroke='#ffffff',width=3) for x,y,r in ((54,58,27),(125,53,34),(103,115,29),(42,126,15),(160,114,15)))
art['BICYCLE']=wheel(40,123)+wheel(153,123)+path('M40 123L74 72L105 123H40L119 77L153 123M105 123L74 72M70 68H91M119 77L111 52H135',stroke=red,width=7)
art['HELMET']=path('M31 97Q25 25 95 24Q161 29 162 97L143 115H51Z',yellow)+path('M47 101L66 144L121 144L144 101',stroke=ink,width=7)+path('M71 36L77 70M95 30V67M120 37L115 70',stroke=wood,width=6)
art['JUMP_ROPE']=path('M47 54C-8 151 198 177 146 53',stroke=blue,width=8)+path('M43 30L53 70M150 29L140 69',stroke=red,width=16)
art['SCOOTER']=wheel(49,136)+wheel(152,136)+path('M49 128H140L115 37H92M115 37L108 20H137',stroke=blue,width=9)
art['HOOP']=ellipse(96,88,65,58,'none').replace('fill="none"','fill="none" stroke="#f29a87" stroke-width="12"')+path('M46 56Q98 21 146 57',stroke=yellow,width=9)
art['GOAL']=path('M26 140V41H142L170 140M142 41V140H26M142 140H170',stroke=cream,width=8)+''.join(line(x,47,x,136,'#91bac2',2) for x in range(45,143,19))+''.join(line(31,y,142,y,'#91bac2',2) for y in range(61,137,19))
art['SUN']=circle(96,86,42,yellow)+''.join(line(96+__import__('math').cos(i*__import__('math').pi/4)*55,86+__import__('math').sin(i*__import__('math').pi/4)*55,96+__import__('math').cos(i*__import__('math').pi/4)*69,86+__import__('math').sin(i*__import__('math').pi/4)*69,yellow,7) for i in range(8))
art['MOON']=path('M116 21C37 22 21 122 94 148Q140 158 164 114C93 134 57 65 116 21',yellow)
art['RAIN']=ellipse(96,64,64,26,'#a6cbdc')+circle(70,49,27,'#bbd9e5')+circle(111,46,32,'#bbd9e5')+''.join(line(x,y,x-7,y+20,blue,5) for x,y in ((48,100),(80,113),(115,100),(146,114)))
art['RAINBOW']=''.join(path(f'M{28+i*10} 139A{68-i*10} {90-i*10} 0 0 1 {164-i*10} 139',stroke=c,width=10) for i,c in enumerate((red,'#f2ac61',yellow,green,blue,'#b898d4')))
art['MUSHROOM']=rect(80,78,33,68,cream)+path('M22 90Q22 23 96 22Q166 24 171 90Z',red)+circle(64,62,12,cream)+circle(115,47,10,cream)+circle(140,79,9,cream)
art['ROCK']=path('M25 112L47 54L108 32L152 67L172 126L117 148L55 140Z','#99a9b3')+path('M47 54L89 90L108 32M89 90L152 67M89 90L117 148',stroke='#c7d2d8',width=4)
art['DOG']=ellipse(97,112,47,29,'#c48d5b')+circle(72,68,35,'#d3a36e')+ellipse(43,68,14,32,'#9f6c45')+ellipse(101,66,13,30,'#9f6c45')+eyes(60,64,23)+ellipse(73,81,18,12,cream)+circle(73,76,6,ink)+rect(62,126,15,24,wood)+rect(117,127,15,23,wood)+path('M140 105Q175 70 161 63',stroke=wood,width=13)
art['TURTLE']=ellipse(98,107,60,35,green)+path('M43 107Q50 43 96 49Q143 44 158 107Z','#6c9e69')+path('M58 73L94 58L130 75L121 106H73Z',stroke='#b8cf80',width=4)+circle(28,103,20,'#a4cb89')+circle(24,98,4,ink)+ellipse(61,142,14,7,green)+ellipse(136,142,14,7,green)
art['ELEPHANT']=ellipse(106,91,58,44,'#98b4c9')+circle(63,75,39,'#9dbbd0')+ellipse(83,77,27,32,'#b4ccdb')+path('M32 84Q8 153 53 140',stroke='#98b4c9',width=19)+circle(44,67,4,ink)+rect(71,111,20,40,'#98b4c9')+rect(133,111,20,40,'#98b4c9')
art['MONKEY']=path('M125 120Q182 153 166 78Q162 56 150 74',stroke=wood,width=12)+ellipse(98,116,35,35,wood)+circle(57,55,19,wood)+circle(131,55,19,wood)+circle(94,63,42,wood)+ellipse(94,71,33,31,'#f0c99b')+eyes(80,63,27)+path('M82 82Q94 93 107 81',stroke=wood,width=3)+line(70,114,42,143,wood,12)+line(121,116,143,145,wood,12)
art['FISH']=path('M131 85L172 47V124Z',yellow)+ellipse(88,87,60,40,blue)+path('M76 49L97 25L113 53M73 124L97 149L108 123',green)+circle(55,77,8,cream)+circle(53,77,4,ink)+path('M84 65Q111 86 84 109',stroke='#28779b',width=4)
art['SNAIL']=path('M24 130Q65 110 124 116L148 84Q169 71 174 98L166 139H24Z',green)+circle(85,89,44,wood)+path('M94 112C39 105 60 44 98 61C134 77 99 111 83 91C74 80 91 72 96 83',stroke=cream,width=6)+line(155,90,148,62,green,5)+line(165,90,174,65,green,5)+circle(148,61,5,ink)+circle(174,64,5,ink)
art['HOUSE']=rect(40,70,117,80,cream)+path('M24 76L96 20L174 76Z',red)+rect(86,103,28,47,wood)+rect(51,86,24,25,blue)+rect(126,86,22,25,blue)
art['SCHOOL']=rect(23,63,147,88,cream)+path('M17 65L96 25L178 65Z',red)+rect(82,103,30,48,wood)+''.join(rect(x,y,20,22,blue,2) for x,y in ((37,80),(136,80),(37,116),(136,116)))+circle(96,65,17,'#fffdf3')+path('M96 53V66H106',stroke=ink,width=3)
art['BRIDGE']=path('M19 109Q97 40 174 109L174 139Q97 75 19 139Z',wood)+path('M19 87Q97 17 174 87',stroke=wood,width=8)+''.join(line(x,83-25*__import__('math').sin((x-20)/155*3.14),x,108-25*__import__('math').sin((x-20)/155*3.14),wood,5) for x in range(22,175,25))+path('M13 150Q51 140 94 150T181 150',stroke=blue,width=6)
art['BENCH']=rect(23,43,146,19,wood)+rect(23,69,146,19,wood)+rect(20,101,151,14,wood)+rect(38,58,11,91,ink)+rect(141,58,11,91,ink)
for name,c in (('BIN',blue),('RECYCLE_BIN',green)):
 art[name]=path('M49 50H145L133 146H61Z',c)+rect(42,35,110,15,ink)+rect(77,24,41,12,ink)+path('M96 70L117 104H77Z',cream)+path('M81 129H113',stroke=cream,width=5)
art['CLOCK']=circle(96,87,65,wood)+circle(96,87,55,cream)+path('M96 49V89L125 107',stroke=ink,width=7)+''.join(circle(96+__import__('math').cos(i*__import__('math').pi/6)*45,87+__import__('math').sin(i*__import__('math').pi/6)*45,2,ink) for i in range(12))
art['COMPASS']=circle(96,87,62,yellow)+circle(96,87,51,cream)+path('M96 36L113 88L79 88Z',red)+path('M96 139L113 88L79 88Z',blue)+circle(96,88,6,ink)+path('M96 32V43M96 131V143M41 87H52M140 87H151',stroke=wood,width=4)
art['MAGNET']=path('M43 41V99C43 165 151 165 151 99V41H117V100C117 123 77 123 77 100V41Z',red)+rect(43,31,34,33,'#c6d3df',2)+rect(117,31,34,33,'#c6d3df',2)
art['GLOBE']=path('M60 28Q157 8 162 81Q166 133 101 140',stroke=wood,width=8)+circle(94,75,52,blue)+path('M60 47L83 33L99 48L91 73L65 78L45 65M105 80L132 68L139 94L115 118L101 98Z',green)+line(96,127,96,144,wood,9)+ellipse(96,149,37,7,wood)
art['TELESCOPE']=path('M40 81L134 34L155 75L60 121Z',blue)+path('M125 28L144 21L170 70L152 80Z',ink)+path('M64 113L95 97L105 116L75 132Z',wood)+path('M104 108L72 153M104 108L137 153M104 108V150',stroke=wood,width=7)
art['THERMOMETER']=rect(78,20,35,104,'#e0edf2',17)+circle(95,128,26,'#e0edf2')+circle(95,129,15,red)+rect(89,54,12,77,red)+''.join(line(105,y,116,y,ink,3) for y in range(42,106,15))
def main():
 OUT.mkdir(parents=True,exist_ok=True)
 specs=json.loads((OUT.parent/'objects.json').read_text())
 assert set(art)=={x['id'] for x in specs}
 for spec in specs:
  name=spec['id'];svg=f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 192 176"><title>{spec["labelVi"]}</title><g stroke-linejoin="round">{art[name]}</g></svg>\n'
  (OUT/(name.lower()+'.svg')).write_text(svg)
 print(f'Authored {len(art)} original SVG illustrations')
if __name__=='__main__':main()
