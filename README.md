# Chat_SD

## Conception 
https://docs.google.com/document/d/1iGLPE0t_NPyegSXGvl0VYDZBLUSgusaZvZ5jt_cloXk/edit


## Doc anneau de Chord

http://graal.ens-lyon.fr/~abenoit/reso05/cours/2-p2p.pdf Partie 2.4


## Sujet

\1. Client de chat distribué

L’objectif est de mettre au point un service de chat distribué pair à pair, et donc sans controleur central. Aucun noeud ne devra avoir de rôle particulier, ou ne connaitra l’ensemble des pairs. Le but est en effet que le système puisse en théorie supporter des milliers (ou des millions) de pairs.

Cet ensemble de pairs sera organisé sous la forme d’un anneau Chord virtuel et dynamique. Le protocole exécuté par les pairs assurera le maintient de cet anneau lors de l’arrivée ou du départ de pairs.

L’accès au système s’effectuera via une ou plusieurs attaches (handles, poignées) qui seront des pairs actifs connus par un serveur fixe et déterminé (l’annuaire). Par exemple, l’annuaire pourrait permettre de connaitre les adresses IPs d’un certains nombre de pairs potentiellement présents dans le système (par exemple les 10 derniers a être entrés).

Nature des pairs::

    On va supposer que

            les pairs sont honnêtes et appliquent le protocole (pas de hacker, d’attaquants, de leechers, de tricheurs).
            les “pannes” (connection réseau trop lente ou interompue, batterie à plat, deconnection brutale ... ) sont supposées assez rares mais probables. À titre d’exemple si on est connecté un à un seul pair il est très probable qu’il disparaisse un jour, mais si on est connecté à 10 pairs (aléatoires) on suppose que leur disparition simultannée (dans les mêmes 10 secondes) est un évènement exceptionnel (en ce cas on se deconnecte aussi et tant pis)

La structure d’un pair

    Un pair sera constitué de deux parties distinctes et indépendantes :

        Un client du système de chat
        son interface (texte, graphique).

L’interaction entre ces deux parties s’effectuera soit via des appels de méthodes soit en modifiant des objets partagés. Vous proposerez au moins une interface de type console mais vous prendrez soin de rendre l’ajout de nouvelles interfaces (Web, Téléphone) facile.

\2. Spécification et implémentation d’un système simplifié (14/20) +4

Dans ce système simplifié vous pouvez :

    Utiliser Un anneau standard (sans cordes) en pratique c’est inutilisable car la recherche d’un pair induit N

    communications, de plus la tolérance aux “pannes” est quasi nulle.
    Supposer que les pairs ne tombent jamais en panne et que toute deconnection est négociée (ie un pair qui s’en va exécute Leave).

*Je vous conseille cependant dès le départ d’utiliser un anneau Chord et de prendre en compte la volatilité des pairs (timeout)

2.1. L’Annuaire (0/20) +2

Vous pouvez utiliser un annuaire ultra-simplfié qui réfère à l’adresse IP/port d’un pair toujours connecté.

Annuaire amélioré : Un tel annuaire est un service qui renvoie un ensemble d’adresses de pairs recemments actifs dans le système. Les pairs souhaitant rejoindre le système le consule et l’annuaire est mis à jour régulierement.

L’ajout d’un tel annuaire donnera un bonus de 1-2 points.

2.2. Spécification du protocole distribué (3/20)

Les communications entre pairs s’effectueront via des communications TCP, vous utiliserez un protocole client serveur spécifique (de la même nature que le protocole HTTP ou l’on envoie des messages de type GET ou POST associé à un ensemble de paramètres). Le pair distant détermine alors les méthodes qu’il doit exécuter ainsi que et leurs arguments en effectuant une analyse lexicale rudimentaire (pattern matching etc... ).

    L’adressage des pair:

        Dans le système on pourra communiquer avec (i.e référencer) un pair soit en utilisant une socket déjà connectée à ce pair, soit en utilisant le couple (adresse-IP, port) sur lequel l’interface de ce pair est active (ie en pratique un SocketServer) est actif sur cette IP et ce port. Le port utilisé par le protocole n’est donc pas fixé – même si bien entendu un port par défaut peut être convenu–.

        Ceci vous permettra d’éxecuter plusieur pairs sur la même machine en leur affectant des ports différents.

        Si vous le souhaitez vous pouvez sérialiser les appels de fonction distants, ceci évite en effet une analyse lexicale (même minime) des messages transmis. Mais en ce cas vous le ferez en utilisant une sérialisation compatibles avec d’autre langages (jyson, xml ou autre).

Quelque soit votre choix d’implémentation :

Spécification et protocole (3points):

        Il vous est demandé de définir un protocole distribué(tel http, ssh, mail,bitorrent) parfaitement indépendant de Java et permettant donc à priori d’utiliser des pairs utilisant des implémentation hétérogènes.
        Les aspects sémantiques du protocole (en langage naturel et informel) seront décrits.
        Votre implémentation en Java devra correspondre au protocole que vous aurez défini.

2.3. Le protocole d’anneau virtuel (3/20)

N.B : la pluspart des méthodes retourneront à Timeout

Les pairs seront organisés sous la forme d’un anneau principal Chord (voir le TP6). Chaque pair aura donc un identifiant codé sur 64 bits (sa clef key) qui déterminera sa place sur l’anneau, celui ci pourra être obtenu en applicant une fonction de hachage à l’IP du pair ou à l’identifiant de l’utilisateur, ou encore être simplement aléatoire.

    Chaque pair maintiendra des références distantes (ou des sockets ouvertes) vers ses prédécesseurs et succeseurs sur l’anneau chord.
    Chaque pair restera accessible afin d’accueillir d’eventuels nouveau pairs dans l’anneau (via un ServerSocket)

Tout pair proposera la méthode (distance) FindMainChord(long key) qui sera appelée par un pair ayant pour clef key souhaitant rejoindre l’anneau. Celle ci retournera l’emplacement de key dans l’anneau (ie d’adresse IP su successeur de key dans l’anneau).

Tout pair p
proposera la méthode JoinMainChord qui inserera le pair p dans l’anneau chord à l’emplacement p.key() et initialisera ses connections (ou réfeŕences distantes) vers son prédecesseur et son successeur. De facon symétrique on définira la méthode LeaveMainCHord celle ci devra mettre à jour les liens de l’anneau virtuel (i,e le prédecesseur de p devra dès lors pointer sur le successeur de p).

Tout pair proposera la méthode ForwardMessage(some data) qui fera circuler un message vers son/ses sucesseurs, on veillera a ne pas transmettre le même message deux fois.

2.4. Les salons de conversation (6/20)

Un salon de conversation sera un sous ensemble des pairs du système. Ainsi chaque pair appartiendra à un ensemble éventuellement vide de salons.

Les fonctionnalités principales utilisateur sont :

    GetChatRoomsList() : renvoie la liste des identifiants des salles de chat.
    JoinChatRoom(long chatkey) : rejoint la salle si elle existe, la crée sinon.
    SendToChatRoom(String s, long chatkey) envoie un message dans le salon chatkey.
    ReadChatRoom(longchatkey) référence à un tampon stockant les messages venant du salon.

Les fonctionnalités protocole seront de

    transmettre ou retransmettre les messages du salon.
    d accueillir les nouveaux venus dans le salon.
    maintenir la structure du salon

On pourra organiser chaque salon trivialement (soit par un graphe complet -chacun connait tous les membres , ou arbre chacun connait un membre) ou un organisation ad-hoc. Cependant je vous conseille une organisation en anneau virtuel Chord comme l’anneau principal. On pourra par exemple utiliser une tache de service (elle même potentiellement multi-thread) par salon, mais ceci n’est pas impératif.

2.5. La gestion des salons (2/20)+2

On pourra opter pour diverses solutions concernant la gestion des salons de discussion:

    La gestion naive (qui ne passe pas à l’echelle) : GetChatRoomsList() innonde le réseau en demandant aux pairs la liste de leur salons, la liste des salles trouvées apparait dynamiquement et à chaque salon découvert est associé un ensemble de pairs a contacter.
    Gestion naive (alternative) : tous les pairs connaissent la liste des salons (pas nécessairement à jour) et pour chaque salon une liste de membres et on essaie de garder ces informations à jour.
    La gestion par annuaire (bonus +2) : on associe a chaque salon S

une clef S.key, et on utilise un dictionnaire distribué Dic porté par l’anneau Chord principal afin de stocker les informations afférentes au salon dans la “case” Dic[S.key]

    . (N.B seule cette méthode passe à l’échelle).
    
\3. Client renforcé (Vrai réseau Chord) 6/20

Le pair de clef key

établira, comme dans un vrai réseau chord, des connections directes vers le pair responsable s:math:key+4^i pour i∈log4(KEYMAX)
.
On modifiera le protocole de recherche de clef afin qu’il utilise les chordes et nécéssite au plus O(logKEYMAX)
envois de message au lien de O(KEYMAX).
On s’assurera que ces connections restent active et si le pair associé à key+4i
ne repond plus on le remplacera en recherchant à nouveau la clef key+4i

\4. Fonctionnalités additionnelles (0/20)+4

Envoi de données structurées (Texte enrichi, musique, images, vidéo) (bonus +2)
Recherche d’un ami et invitation de celui-ci (les participants déposeront leur surnom dans la table de hachage, bien entendu il faudra connaitre le surnom de l’utilisateur a inviter (bonus +2)




