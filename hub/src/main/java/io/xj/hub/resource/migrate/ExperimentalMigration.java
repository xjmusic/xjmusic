package io.xj.hub.resource.migrate;

import io.xj.core.access.Access;
import io.xj.core.dao.DAORecord;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.model.InstrumentAudioChord;
import io.xj.core.model.InstrumentAudioEvent;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.model.Library;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramMeme;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.core.model.ProgramSequencePattern;
import io.xj.core.model.ProgramSequencePatternEvent;
import io.xj.core.model.ProgramVoice;
import io.xj.core.model.ProgramVoiceTrack;
import io.xj.core.model.User;
import io.xj.core.model.UserRole;
import org.jooq.DSLContext;

public class ExperimentalMigration {

  private final DSLContext db;
  private final Access access;
  User user1;
  User user2;
  User user3;
  User user4;
  User user5;
  User user6;
  User user7;
  User user8;
  User user9;
  User user10;
  User user11;
  User user12;
  User user13;
  User user14;
  User user15;
  User user16;
  User user17;
  User user18;
  User user19;
  User user20;
  User user21;
  User user22;
  User user23;
  User user24;
  User user25;
  User user26;
  User user27;
  User user28;
  User user29;
  User user30;
  User user31;
  User user32;
  User user33;
  User user34;
  User user35;
  User user36;
  User user37;
  User user38;
  User user39;
  User user40;
  User user41;
  Account account1;
  Account account2;
  Account account4;
  Account account5;
  Account account6;
  Library library1;
  Library library3;
  Library library4;
  Library library5;
  Instrument instrument9;
  InstrumentAudio audio0;
  InstrumentAudioEvent audioEvent1;
  InstrumentAudioChord audioChord2;
  InstrumentAudio audio3;
  InstrumentAudioEvent audioEvent4;
  InstrumentAudioChord audioChord5;
  Instrument instrument12;
  InstrumentAudio audio6;
  InstrumentAudioEvent audioEvent7;
  InstrumentAudio audio8;
  InstrumentAudioEvent audioEvent9;
  InstrumentAudio audio10;
  InstrumentAudioEvent audioEvent11;
  Instrument instrument8;
  InstrumentAudio audio12;
  InstrumentAudioEvent audioEvent13;
  InstrumentAudioChord audioChord14;
  Instrument instrument10;
  InstrumentAudio audio15;
  InstrumentAudioEvent audioEvent16;
  InstrumentAudioChord audioChord17;
  Instrument instrument7;
  InstrumentAudio audio18;
  InstrumentAudioEvent audioEvent19;
  InstrumentAudioChord audioChord20;
  InstrumentAudio audio21;
  InstrumentAudioEvent audioEvent22;
  InstrumentAudioChord audioChord23;
  InstrumentAudio audio24;
  InstrumentAudioEvent audioEvent25;
  Instrument instrument4;
  InstrumentAudio audio26;
  InstrumentAudioEvent audioEvent27;
  InstrumentAudio audio28;
  InstrumentAudioEvent audioEvent29;
  InstrumentAudio audio30;
  InstrumentAudio audio31;
  InstrumentAudioEvent audioEvent32;
  InstrumentAudio audio33;
  InstrumentAudioEvent audioEvent34;
  InstrumentAudio audio35;
  InstrumentAudioEvent audioEvent36;
  InstrumentAudio audio37;
  InstrumentAudioEvent audioEvent38;
  InstrumentAudio audio39;
  InstrumentAudioEvent audioEvent40;
  InstrumentAudio audio41;
  InstrumentAudioEvent audioEvent42;
  InstrumentAudio audio43;
  InstrumentAudioEvent audioEvent44;
  InstrumentAudio audio45;
  InstrumentAudioEvent audioEvent46;
  InstrumentAudio audio47;
  InstrumentAudioEvent audioEvent48;
  InstrumentAudio audio49;
  InstrumentAudioEvent audioEvent50;
  InstrumentAudio audio51;
  InstrumentAudioEvent audioEvent52;
  InstrumentAudio audio53;
  InstrumentAudioEvent audioEvent54;
  InstrumentAudio audio55;
  InstrumentAudioEvent audioEvent56;
  InstrumentAudio audio57;
  InstrumentAudioEvent audioEvent58;
  InstrumentAudio audio59;
  InstrumentAudio audio60;
  InstrumentAudioEvent audioEvent61;
  InstrumentAudio audio62;
  InstrumentAudioEvent audioEvent63;
  InstrumentAudio audio64;
  InstrumentAudioEvent audioEvent65;
  InstrumentAudio audio66;
  InstrumentAudioEvent audioEvent67;
  InstrumentAudio audio68;
  InstrumentAudioEvent audioEvent69;
  InstrumentAudio audio70;
  InstrumentAudioEvent audioEvent71;
  InstrumentAudio audio72;
  InstrumentAudioEvent audioEvent73;
  InstrumentAudio audio74;
  InstrumentAudioEvent audioEvent75;
  InstrumentAudio audio76;
  InstrumentAudioEvent audioEvent77;
  InstrumentAudio audio78;
  InstrumentAudioEvent audioEvent79;
  InstrumentAudio audio80;
  InstrumentAudioEvent audioEvent81;
  InstrumentAudio audio82;
  InstrumentAudioEvent audioEvent83;
  InstrumentAudio audio84;
  InstrumentAudioEvent audioEvent85;
  InstrumentAudio audio86;
  InstrumentAudioEvent audioEvent87;
  InstrumentAudio audio88;
  InstrumentAudioEvent audioEvent89;
  InstrumentAudio audio90;
  InstrumentAudioEvent audioEvent91;
  InstrumentAudio audio92;
  InstrumentAudioEvent audioEvent93;
  InstrumentAudio audio94;
  InstrumentAudio audio95;
  InstrumentAudioEvent audioEvent96;
  InstrumentAudio audio97;
  InstrumentAudio audio98;
  InstrumentAudioEvent audioEvent99;
  InstrumentAudio audio100;
  InstrumentAudioEvent audioEvent101;
  InstrumentAudio audio102;
  InstrumentAudioEvent audioEvent103;
  InstrumentAudio audio104;
  InstrumentAudioEvent audioEvent105;
  InstrumentAudio audio106;
  InstrumentAudioEvent audioEvent107;
  InstrumentAudio audio108;
  InstrumentAudio audio109;
  InstrumentAudio audio110;
  InstrumentAudioEvent audioEvent111;
  InstrumentAudio audio112;
  InstrumentAudioEvent audioEvent113;
  InstrumentAudio audio114;
  InstrumentAudioEvent audioEvent115;
  Instrument instrument33;
  InstrumentAudio audio116;
  InstrumentAudioEvent audioEvent117;
  InstrumentAudio audio118;
  InstrumentAudioEvent audioEvent119;
  InstrumentAudio audio120;
  InstrumentAudioEvent audioEvent121;
  InstrumentAudio audio122;
  InstrumentAudioEvent audioEvent123;
  InstrumentAudio audio124;
  InstrumentAudioEvent audioEvent125;
  InstrumentAudio audio126;
  InstrumentAudioEvent audioEvent127;
  InstrumentAudio audio128;
  InstrumentAudioEvent audioEvent129;
  InstrumentAudio audio130;
  InstrumentAudioEvent audioEvent131;
  InstrumentAudio audio132;
  InstrumentAudioEvent audioEvent133;
  InstrumentAudio audio134;
  InstrumentAudioEvent audioEvent135;
  InstrumentAudio audio136;
  InstrumentAudioEvent audioEvent137;
  InstrumentAudio audio138;
  InstrumentAudioEvent audioEvent139;
  InstrumentAudio audio140;
  InstrumentAudioEvent audioEvent141;
  InstrumentAudio audio142;
  InstrumentAudioEvent audioEvent143;
  InstrumentAudio audio144;
  InstrumentAudioEvent audioEvent145;
  InstrumentAudio audio146;
  InstrumentAudioEvent audioEvent147;
  InstrumentAudio audio148;
  InstrumentAudioEvent audioEvent149;
  InstrumentAudio audio150;
  InstrumentAudioEvent audioEvent151;
  InstrumentAudio audio152;
  InstrumentAudio audio153;
  InstrumentAudioEvent audioEvent154;
  InstrumentAudio audio155;
  InstrumentAudioEvent audioEvent156;
  InstrumentAudio audio157;
  InstrumentAudioEvent audioEvent158;
  InstrumentAudio audio159;
  InstrumentAudioEvent audioEvent160;
  InstrumentAudio audio161;
  InstrumentAudioEvent audioEvent162;
  InstrumentAudio audio163;
  InstrumentAudioEvent audioEvent164;
  InstrumentAudio audio165;
  InstrumentAudioEvent audioEvent166;
  InstrumentAudio audio167;
  InstrumentAudioEvent audioEvent168;
  InstrumentAudio audio169;
  InstrumentAudioEvent audioEvent170;
  InstrumentAudio audio171;
  InstrumentAudioEvent audioEvent172;
  InstrumentAudio audio173;
  InstrumentAudioEvent audioEvent174;
  InstrumentAudio audio175;
  InstrumentAudioEvent audioEvent176;
  InstrumentAudio audio177;
  InstrumentAudioEvent audioEvent178;
  InstrumentAudio audio179;
  InstrumentAudioEvent audioEvent180;
  Instrument instrument34;
  InstrumentAudio audio181;
  InstrumentAudioEvent audioEvent182;
  InstrumentAudio audio183;
  InstrumentAudioEvent audioEvent184;
  InstrumentAudio audio185;
  InstrumentAudioEvent audioEvent186;
  InstrumentAudio audio187;
  InstrumentAudioEvent audioEvent188;
  InstrumentAudio audio189;
  InstrumentAudioEvent audioEvent190;
  InstrumentAudio audio191;
  InstrumentAudioEvent audioEvent192;
  InstrumentAudio audio193;
  InstrumentAudioEvent audioEvent194;
  InstrumentAudio audio195;
  InstrumentAudioEvent audioEvent196;
  InstrumentAudio audio197;
  InstrumentAudioEvent audioEvent198;
  InstrumentAudio audio199;
  InstrumentAudioEvent audioEvent200;
  InstrumentAudio audio201;
  InstrumentAudioEvent audioEvent202;
  InstrumentAudio audio203;
  InstrumentAudioEvent audioEvent204;
  InstrumentAudio audio205;
  InstrumentAudioEvent audioEvent206;
  InstrumentAudio audio207;
  InstrumentAudioEvent audioEvent208;
  InstrumentAudio audio209;
  InstrumentAudioEvent audioEvent210;
  InstrumentAudio audio211;
  InstrumentAudioEvent audioEvent212;
  InstrumentAudio audio213;
  InstrumentAudioEvent audioEvent214;
  InstrumentAudio audio215;
  InstrumentAudioEvent audioEvent216;
  InstrumentAudio audio217;
  InstrumentAudioEvent audioEvent218;
  InstrumentAudio audio219;
  InstrumentAudioEvent audioEvent220;
  InstrumentAudio audio221;
  InstrumentAudioEvent audioEvent222;
  InstrumentAudio audio223;
  InstrumentAudioEvent audioEvent224;
  InstrumentAudio audio225;
  InstrumentAudioEvent audioEvent226;
  Instrument instrument32;
  InstrumentAudio audio227;
  InstrumentAudioEvent audioEvent228;
  InstrumentAudio audio229;
  InstrumentAudioEvent audioEvent230;
  InstrumentAudio audio231;
  InstrumentAudioEvent audioEvent232;
  InstrumentAudio audio233;
  InstrumentAudioEvent audioEvent234;
  InstrumentAudio audio235;
  InstrumentAudioEvent audioEvent236;
  InstrumentAudio audio237;
  InstrumentAudioEvent audioEvent238;
  InstrumentAudio audio239;
  InstrumentAudioEvent audioEvent240;
  InstrumentAudio audio241;
  InstrumentAudioEvent audioEvent242;
  InstrumentAudio audio243;
  InstrumentAudioEvent audioEvent244;
  InstrumentAudio audio245;
  InstrumentAudioEvent audioEvent246;
  InstrumentAudio audio247;
  InstrumentAudioEvent audioEvent248;
  InstrumentAudio audio249;
  InstrumentAudioEvent audioEvent250;
  InstrumentAudio audio251;
  InstrumentAudioEvent audioEvent252;
  InstrumentAudio audio253;
  InstrumentAudioEvent audioEvent254;
  InstrumentAudio audio255;
  InstrumentAudioEvent audioEvent256;
  InstrumentAudio audio257;
  InstrumentAudioEvent audioEvent258;
  InstrumentAudio audio259;
  InstrumentAudioEvent audioEvent260;
  InstrumentAudio audio261;
  InstrumentAudioEvent audioEvent262;
  InstrumentAudio audio263;
  InstrumentAudioEvent audioEvent264;
  InstrumentAudio audio265;
  InstrumentAudioEvent audioEvent266;
  InstrumentAudio audio267;
  InstrumentAudioEvent audioEvent268;
  InstrumentAudio audio269;
  InstrumentAudioEvent audioEvent270;
  InstrumentAudio audio271;
  InstrumentAudioEvent audioEvent272;
  InstrumentAudio audio273;
  InstrumentAudioEvent audioEvent274;
  InstrumentAudio audio275;
  InstrumentAudioEvent audioEvent276;
  InstrumentAudio audio277;
  InstrumentAudioEvent audioEvent278;
  InstrumentAudio audio279;
  InstrumentAudioEvent audioEvent280;
  InstrumentAudio audio281;
  InstrumentAudioEvent audioEvent282;
  InstrumentAudio audio283;
  InstrumentAudioEvent audioEvent284;
  InstrumentAudio audio285;
  InstrumentAudioEvent audioEvent286;
  InstrumentAudio audio287;
  InstrumentAudioEvent audioEvent288;
  InstrumentAudio audio289;
  InstrumentAudioEvent audioEvent290;
  InstrumentAudio audio291;
  InstrumentAudioEvent audioEvent292;
  InstrumentAudio audio293;
  InstrumentAudioEvent audioEvent294;
  Instrument instrument28;
  InstrumentAudio audio295;
  InstrumentAudioEvent audioEvent296;
  InstrumentAudio audio297;
  InstrumentAudioEvent audioEvent298;
  InstrumentAudio audio299;
  InstrumentAudioEvent audioEvent300;
  InstrumentAudio audio301;
  InstrumentAudioEvent audioEvent302;
  InstrumentAudio audio303;
  InstrumentAudioEvent audioEvent304;
  InstrumentAudio audio305;
  InstrumentAudioEvent audioEvent306;
  InstrumentAudio audio307;
  InstrumentAudioEvent audioEvent308;
  InstrumentAudio audio309;
  InstrumentAudioEvent audioEvent310;
  InstrumentAudio audio311;
  InstrumentAudioEvent audioEvent312;
  InstrumentAudio audio313;
  InstrumentAudioEvent audioEvent314;
  InstrumentAudio audio315;
  InstrumentAudioEvent audioEvent316;
  InstrumentAudio audio317;
  InstrumentAudioEvent audioEvent318;
  InstrumentAudio audio319;
  InstrumentAudioEvent audioEvent320;
  InstrumentAudio audio321;
  InstrumentAudioEvent audioEvent322;
  InstrumentAudio audio323;
  InstrumentAudioEvent audioEvent324;
  InstrumentAudio audio325;
  InstrumentAudioEvent audioEvent326;
  InstrumentAudio audio327;
  InstrumentAudioEvent audioEvent328;
  InstrumentAudio audio329;
  InstrumentAudioEvent audioEvent330;
  InstrumentAudio audio331;
  InstrumentAudioEvent audioEvent332;
  InstrumentAudio audio333;
  InstrumentAudioEvent audioEvent334;
  InstrumentAudio audio335;
  InstrumentAudioEvent audioEvent336;
  InstrumentAudio audio337;
  InstrumentAudioEvent audioEvent338;
  InstrumentAudio audio339;
  InstrumentAudioEvent audioEvent340;
  InstrumentAudio audio341;
  InstrumentAudioEvent audioEvent342;
  InstrumentAudio audio343;
  InstrumentAudioEvent audioEvent344;
  Instrument instrument3;
  InstrumentAudio audio345;
  InstrumentAudioEvent audioEvent346;
  InstrumentAudio audio347;
  InstrumentAudioEvent audioEvent348;
  InstrumentAudio audio349;
  InstrumentAudioEvent audioEvent350;
  InstrumentAudio audio351;
  InstrumentAudioEvent audioEvent352;
  InstrumentAudio audio353;
  InstrumentAudioEvent audioEvent354;
  InstrumentAudio audio355;
  InstrumentAudioEvent audioEvent356;
  InstrumentAudio audio357;
  InstrumentAudioEvent audioEvent358;
  InstrumentAudio audio359;
  InstrumentAudioEvent audioEvent360;
  InstrumentAudio audio361;
  InstrumentAudioEvent audioEvent362;
  InstrumentAudio audio363;
  InstrumentAudioEvent audioEvent364;
  InstrumentAudio audio365;
  InstrumentAudioEvent audioEvent366;
  InstrumentAudio audio367;
  InstrumentAudioEvent audioEvent368;
  InstrumentAudio audio369;
  InstrumentAudioEvent audioEvent370;
  InstrumentAudio audio371;
  InstrumentAudioEvent audioEvent372;
  InstrumentAudio audio373;
  InstrumentAudioEvent audioEvent374;
  InstrumentAudio audio375;
  InstrumentAudioEvent audioEvent376;
  InstrumentAudio audio377;
  InstrumentAudioEvent audioEvent378;
  InstrumentAudio audio379;
  InstrumentAudioEvent audioEvent380;
  InstrumentAudio audio381;
  InstrumentAudioEvent audioEvent382;
  InstrumentAudio audio383;
  InstrumentAudioEvent audioEvent384;
  InstrumentAudio audio385;
  InstrumentAudioEvent audioEvent386;
  InstrumentAudio audio387;
  InstrumentAudioEvent audioEvent388;
  InstrumentAudio audio389;
  InstrumentAudioEvent audioEvent390;
  InstrumentAudio audio391;
  InstrumentAudioEvent audioEvent392;
  InstrumentAudio audio393;
  InstrumentAudioEvent audioEvent394;
  InstrumentAudio audio395;
  InstrumentAudioEvent audioEvent396;
  InstrumentAudio audio397;
  InstrumentAudioEvent audioEvent398;
  InstrumentAudio audio399;
  InstrumentAudioEvent audioEvent400;
  InstrumentAudio audio401;
  InstrumentAudioEvent audioEvent402;
  InstrumentAudio audio403;
  InstrumentAudioEvent audioEvent404;
  InstrumentAudio audio405;
  InstrumentAudio audio406;
  InstrumentAudioEvent audioEvent407;
  Instrument instrument35;
  InstrumentAudio audio408;
  InstrumentAudioEvent audioEvent409;
  InstrumentAudio audio410;
  InstrumentAudioEvent audioEvent411;
  InstrumentAudio audio412;
  InstrumentAudioEvent audioEvent413;
  InstrumentAudio audio414;
  InstrumentAudioEvent audioEvent415;
  InstrumentAudio audio416;
  InstrumentAudioEvent audioEvent417;
  InstrumentAudio audio418;
  InstrumentAudioEvent audioEvent419;
  InstrumentAudio audio420;
  InstrumentAudioEvent audioEvent421;
  InstrumentAudio audio422;
  InstrumentAudioEvent audioEvent423;
  InstrumentAudio audio424;
  InstrumentAudioEvent audioEvent425;
  InstrumentAudio audio426;
  InstrumentAudioEvent audioEvent427;
  InstrumentAudio audio428;
  InstrumentAudioEvent audioEvent429;
  InstrumentAudio audio430;
  InstrumentAudioEvent audioEvent431;
  InstrumentAudio audio432;
  InstrumentAudioEvent audioEvent433;
  InstrumentAudio audio434;
  InstrumentAudioEvent audioEvent435;
  InstrumentAudio audio436;
  InstrumentAudioEvent audioEvent437;
  InstrumentAudio audio438;
  InstrumentAudioEvent audioEvent439;
  InstrumentAudio audio440;
  InstrumentAudioEvent audioEvent441;
  InstrumentAudio audio442;
  InstrumentAudioEvent audioEvent443;
  InstrumentAudio audio444;
  InstrumentAudioEvent audioEvent445;
  InstrumentAudio audio446;
  InstrumentAudioEvent audioEvent447;
  InstrumentAudio audio448;
  InstrumentAudioEvent audioEvent449;
  InstrumentAudio audio450;
  InstrumentAudioEvent audioEvent451;
  InstrumentAudio audio452;
  InstrumentAudioEvent audioEvent453;
  Instrument instrument41;
  InstrumentAudio audio454;
  InstrumentAudioEvent audioEvent455;
  InstrumentAudio audio456;
  InstrumentAudioEvent audioEvent457;
  InstrumentAudio audio458;
  InstrumentAudioEvent audioEvent459;
  InstrumentAudio audio460;
  InstrumentAudioEvent audioEvent461;
  InstrumentAudio audio462;
  InstrumentAudioEvent audioEvent463;
  InstrumentAudio audio464;
  InstrumentAudioEvent audioEvent465;
  InstrumentAudio audio466;
  InstrumentAudioEvent audioEvent467;
  InstrumentAudio audio468;
  InstrumentAudioEvent audioEvent469;
  InstrumentAudio audio470;
  InstrumentAudioEvent audioEvent471;
  InstrumentAudio audio472;
  InstrumentAudioEvent audioEvent473;
  InstrumentAudio audio474;
  InstrumentAudioEvent audioEvent475;
  InstrumentAudio audio476;
  InstrumentAudioEvent audioEvent477;
  InstrumentAudio audio478;
  InstrumentAudioEvent audioEvent479;
  InstrumentAudio audio480;
  InstrumentAudioEvent audioEvent481;
  InstrumentAudio audio482;
  InstrumentAudioEvent audioEvent483;
  InstrumentAudio audio484;
  InstrumentAudioEvent audioEvent485;
  InstrumentAudio audio486;
  InstrumentAudioEvent audioEvent487;
  InstrumentAudio audio488;
  InstrumentAudioEvent audioEvent489;
  InstrumentAudio audio490;
  InstrumentAudioEvent audioEvent491;
  InstrumentAudio audio492;
  InstrumentAudioEvent audioEvent493;
  InstrumentAudio audio494;
  InstrumentAudioEvent audioEvent495;
  InstrumentAudio audio496;
  InstrumentAudioEvent audioEvent497;
  InstrumentAudio audio498;
  InstrumentAudioEvent audioEvent499;
  InstrumentAudio audio500;
  InstrumentAudioEvent audioEvent501;
  InstrumentAudio audio502;
  InstrumentAudioEvent audioEvent503;
  InstrumentAudio audio504;
  InstrumentAudioEvent audioEvent505;
  InstrumentAudio audio506;
  InstrumentAudioEvent audioEvent507;
  InstrumentAudio audio508;
  InstrumentAudioEvent audioEvent509;
  InstrumentAudio audio510;
  InstrumentAudioEvent audioEvent511;
  InstrumentAudio audio512;
  InstrumentAudioEvent audioEvent513;
  InstrumentAudio audio514;
  InstrumentAudioEvent audioEvent515;
  InstrumentAudio audio516;
  InstrumentAudioEvent audioEvent517;
  InstrumentAudio audio518;
  InstrumentAudioEvent audioEvent519;
  InstrumentAudio audio520;
  InstrumentAudioEvent audioEvent521;
  InstrumentAudio audio522;
  InstrumentAudioEvent audioEvent523;
  InstrumentAudio audio524;
  InstrumentAudioEvent audioEvent525;
  InstrumentAudio audio526;
  InstrumentAudioEvent audioEvent527;
  InstrumentAudio audio528;
  InstrumentAudioEvent audioEvent529;
  InstrumentAudio audio530;
  InstrumentAudioEvent audioEvent531;
  InstrumentAudio audio532;
  InstrumentAudioEvent audioEvent533;
  InstrumentAudio audio534;
  InstrumentAudioEvent audioEvent535;
  InstrumentAudio audio536;
  InstrumentAudioEvent audioEvent537;
  InstrumentAudio audio538;
  InstrumentAudioEvent audioEvent539;
  InstrumentAudio audio540;
  InstrumentAudioEvent audioEvent541;
  InstrumentAudio audio542;
  InstrumentAudioEvent audioEvent543;
  InstrumentAudio audio544;
  InstrumentAudioEvent audioEvent545;
  InstrumentAudio audio546;
  InstrumentAudioEvent audioEvent547;
  InstrumentAudio audio548;
  InstrumentAudioEvent audioEvent549;
  InstrumentAudio audio550;
  InstrumentAudioEvent audioEvent551;
  InstrumentAudio audio552;
  InstrumentAudioEvent audioEvent553;
  InstrumentAudio audio554;
  InstrumentAudioEvent audioEvent555;
  InstrumentAudio audio556;
  InstrumentAudioEvent audioEvent557;
  InstrumentAudio audio558;
  InstrumentAudioEvent audioEvent559;
  InstrumentAudio audio560;
  InstrumentAudioEvent audioEvent561;
  InstrumentAudio audio562;
  InstrumentAudioEvent audioEvent563;
  InstrumentAudio audio564;
  InstrumentAudioEvent audioEvent565;
  InstrumentAudio audio566;
  InstrumentAudioEvent audioEvent567;
  InstrumentAudio audio568;
  InstrumentAudioEvent audioEvent569;
  InstrumentAudio audio570;
  InstrumentAudioEvent audioEvent571;
  InstrumentAudio audio572;
  InstrumentAudioEvent audioEvent573;
  InstrumentAudio audio574;
  InstrumentAudioEvent audioEvent575;
  InstrumentAudio audio576;
  InstrumentAudioEvent audioEvent577;
  InstrumentAudio audio578;
  InstrumentAudioEvent audioEvent579;
  InstrumentAudio audio580;
  InstrumentAudioEvent audioEvent581;
  InstrumentAudio audio582;
  InstrumentAudioEvent audioEvent583;
  InstrumentAudio audio584;
  InstrumentAudioEvent audioEvent585;
  InstrumentAudio audio586;
  InstrumentAudioEvent audioEvent587;
  InstrumentAudio audio588;
  InstrumentAudioEvent audioEvent589;
  InstrumentAudio audio590;
  InstrumentAudioEvent audioEvent591;
  InstrumentAudio audio592;
  InstrumentAudioEvent audioEvent593;
  InstrumentAudio audio594;
  InstrumentAudioEvent audioEvent595;
  InstrumentAudio audio596;
  InstrumentAudioEvent audioEvent597;
  InstrumentAudio audio598;
  InstrumentAudioEvent audioEvent599;
  InstrumentAudio audio600;
  InstrumentAudioEvent audioEvent601;
  InstrumentAudio audio602;
  InstrumentAudioEvent audioEvent603;
  InstrumentAudio audio604;
  InstrumentAudioEvent audioEvent605;
  InstrumentAudio audio606;
  InstrumentAudioEvent audioEvent607;
  InstrumentAudio audio608;
  InstrumentAudioEvent audioEvent609;
  InstrumentAudio audio610;
  InstrumentAudioEvent audioEvent611;
  InstrumentAudio audio612;
  InstrumentAudioEvent audioEvent613;
  InstrumentAudio audio614;
  InstrumentAudioEvent audioEvent615;
  InstrumentAudio audio616;
  InstrumentAudioEvent audioEvent617;
  InstrumentAudio audio618;
  InstrumentAudioEvent audioEvent619;
  InstrumentAudio audio620;
  InstrumentAudioEvent audioEvent621;
  InstrumentAudio audio622;
  InstrumentAudioEvent audioEvent623;
  InstrumentAudio audio624;
  InstrumentAudioEvent audioEvent625;
  InstrumentAudio audio626;
  InstrumentAudioEvent audioEvent627;
  InstrumentAudio audio628;
  InstrumentAudioEvent audioEvent629;
  InstrumentAudio audio630;
  InstrumentAudioEvent audioEvent631;
  InstrumentAudio audio632;
  InstrumentAudioEvent audioEvent633;
  InstrumentAudio audio634;
  InstrumentAudioEvent audioEvent635;
  InstrumentAudio audio636;
  InstrumentAudioEvent audioEvent637;
  InstrumentAudio audio638;
  InstrumentAudioEvent audioEvent639;
  InstrumentAudio audio640;
  InstrumentAudioEvent audioEvent641;
  InstrumentAudio audio642;
  InstrumentAudioEvent audioEvent643;
  InstrumentAudio audio644;
  InstrumentAudioEvent audioEvent645;
  InstrumentAudio audio646;
  InstrumentAudioEvent audioEvent647;
  InstrumentAudio audio648;
  InstrumentAudioEvent audioEvent649;
  InstrumentAudio audio650;
  InstrumentAudioEvent audioEvent651;
  InstrumentAudio audio652;
  InstrumentAudioEvent audioEvent653;
  InstrumentAudio audio654;
  InstrumentAudioEvent audioEvent655;
  InstrumentAudio audio656;
  InstrumentAudioEvent audioEvent657;
  InstrumentAudio audio658;
  InstrumentAudioEvent audioEvent659;
  InstrumentAudio audio660;
  InstrumentAudioEvent audioEvent661;
  InstrumentAudio audio662;
  InstrumentAudioEvent audioEvent663;
  InstrumentAudio audio664;
  InstrumentAudioEvent audioEvent665;
  InstrumentAudio audio666;
  InstrumentAudioEvent audioEvent667;
  InstrumentAudio audio668;
  InstrumentAudioEvent audioEvent669;
  Instrument instrument29;
  InstrumentAudio audio670;
  InstrumentAudioEvent audioEvent671;
  InstrumentAudio audio672;
  InstrumentAudioEvent audioEvent673;
  InstrumentAudio audio674;
  InstrumentAudioEvent audioEvent675;
  InstrumentAudio audio676;
  InstrumentAudioEvent audioEvent677;
  InstrumentAudio audio678;
  InstrumentAudioEvent audioEvent679;
  InstrumentAudio audio680;
  InstrumentAudioEvent audioEvent681;
  InstrumentAudio audio682;
  InstrumentAudioEvent audioEvent683;
  InstrumentAudio audio684;
  InstrumentAudioEvent audioEvent685;
  InstrumentAudio audio686;
  InstrumentAudioEvent audioEvent687;
  InstrumentAudio audio688;
  InstrumentAudioEvent audioEvent689;
  InstrumentAudio audio690;
  InstrumentAudioEvent audioEvent691;
  InstrumentAudio audio692;
  InstrumentAudioEvent audioEvent693;
  InstrumentAudio audio694;
  InstrumentAudioEvent audioEvent695;
  InstrumentAudio audio696;
  InstrumentAudioEvent audioEvent697;
  InstrumentAudio audio698;
  InstrumentAudioEvent audioEvent699;
  InstrumentAudio audio700;
  InstrumentAudioEvent audioEvent701;
  InstrumentAudio audio702;
  InstrumentAudioEvent audioEvent703;
  InstrumentAudio audio704;
  InstrumentAudioEvent audioEvent705;
  InstrumentAudio audio706;
  InstrumentAudioEvent audioEvent707;
  InstrumentAudio audio708;
  InstrumentAudioEvent audioEvent709;
  InstrumentAudio audio710;
  InstrumentAudioEvent audioEvent711;
  InstrumentAudio audio712;
  InstrumentAudioEvent audioEvent713;
  InstrumentAudio audio714;
  InstrumentAudioEvent audioEvent715;
  InstrumentAudio audio716;
  InstrumentAudioEvent audioEvent717;
  InstrumentAudio audio718;
  InstrumentAudioEvent audioEvent719;
  Instrument instrument25;
  InstrumentAudio audio720;
  InstrumentAudioEvent audioEvent721;
  Instrument instrument42;
  InstrumentAudio audio722;
  InstrumentAudioEvent audioEvent723;
  InstrumentAudio audio724;
  InstrumentAudioEvent audioEvent725;
  InstrumentAudio audio726;
  InstrumentAudioEvent audioEvent727;
  InstrumentAudio audio728;
  InstrumentAudioEvent audioEvent729;
  InstrumentAudio audio730;
  InstrumentAudioEvent audioEvent731;
  InstrumentAudio audio732;
  InstrumentAudioEvent audioEvent733;
  InstrumentAudio audio734;
  InstrumentAudioEvent audioEvent735;
  InstrumentAudio audio736;
  InstrumentAudioEvent audioEvent737;
  InstrumentAudio audio738;
  InstrumentAudioEvent audioEvent739;
  InstrumentAudio audio740;
  InstrumentAudioEvent audioEvent741;
  InstrumentAudioEvent audioEvent742;
  InstrumentAudio audio743;
  InstrumentAudioEvent audioEvent744;
  InstrumentAudio audio745;
  InstrumentAudioEvent audioEvent746;
  InstrumentAudio audio747;
  InstrumentAudioEvent audioEvent748;
  InstrumentAudio audio749;
  InstrumentAudioEvent audioEvent750;
  InstrumentAudio audio751;
  InstrumentAudioEvent audioEvent752;
  InstrumentAudio audio753;
  InstrumentAudioEvent audioEvent754;
  InstrumentAudio audio755;
  InstrumentAudioEvent audioEvent756;
  InstrumentAudio audio757;
  InstrumentAudioEvent audioEvent758;
  InstrumentAudio audio759;
  InstrumentAudioEvent audioEvent760;
  InstrumentAudio audio761;
  InstrumentAudioEvent audioEvent762;
  InstrumentAudio audio763;
  InstrumentAudioEvent audioEvent764;
  InstrumentAudio audio765;
  InstrumentAudioEvent audioEvent766;
  InstrumentAudio audio767;
  InstrumentAudioEvent audioEvent768;
  InstrumentAudio audio769;
  InstrumentAudioEvent audioEvent770;
  InstrumentAudio audio771;
  InstrumentAudioEvent audioEvent772;
  InstrumentAudio audio773;
  InstrumentAudioEvent audioEvent774;
  InstrumentAudio audio775;
  InstrumentAudioEvent audioEvent776;
  Instrument instrument5;
  InstrumentAudio audio777;
  InstrumentAudioEvent audioEvent778;
  InstrumentAudio audio779;
  InstrumentAudioEvent audioEvent780;
  InstrumentAudio audio781;
  InstrumentAudioEvent audioEvent782;
  InstrumentAudio audio783;
  InstrumentAudioEvent audioEvent784;
  InstrumentAudio audio785;
  InstrumentAudioEvent audioEvent786;
  InstrumentAudio audio787;
  InstrumentAudioEvent audioEvent788;
  InstrumentAudio audio789;
  InstrumentAudioEvent audioEvent790;
  InstrumentAudio audio791;
  InstrumentAudioEvent audioEvent792;
  InstrumentAudio audio793;
  InstrumentAudioEvent audioEvent794;
  InstrumentAudio audio795;
  InstrumentAudioEvent audioEvent796;
  InstrumentAudio audio797;
  InstrumentAudioEvent audioEvent798;
  InstrumentAudio audio799;
  InstrumentAudioEvent audioEvent800;
  InstrumentAudio audio801;
  InstrumentAudioEvent audioEvent802;
  InstrumentAudio audio803;
  InstrumentAudioEvent audioEvent804;
  InstrumentAudio audio805;
  InstrumentAudioEvent audioEvent806;
  InstrumentAudio audio807;
  InstrumentAudioEvent audioEvent808;
  InstrumentAudio audio809;
  InstrumentAudioEvent audioEvent810;
  InstrumentAudio audio811;
  InstrumentAudioEvent audioEvent812;
  InstrumentAudio audio813;
  InstrumentAudioEvent audioEvent814;
  InstrumentAudio audio815;
  InstrumentAudioEvent audioEvent816;
  InstrumentAudio audio817;
  InstrumentAudioEvent audioEvent818;
  InstrumentAudio audio819;
  InstrumentAudioEvent audioEvent820;
  InstrumentAudio audio821;
  InstrumentAudioEvent audioEvent822;
  InstrumentAudio audio823;
  InstrumentAudioEvent audioEvent824;
  InstrumentAudio audio825;
  InstrumentAudioEvent audioEvent826;
  InstrumentAudio audio827;
  InstrumentAudioEvent audioEvent828;
  InstrumentAudio audio829;
  InstrumentAudioEvent audioEvent830;
  InstrumentAudio audio831;
  InstrumentAudioEvent audioEvent832;
  InstrumentAudio audio833;
  InstrumentAudioEvent audioEvent834;
  InstrumentAudio audio835;
  InstrumentAudioEvent audioEvent836;
  InstrumentAudio audio837;
  InstrumentAudioEvent audioEvent838;
  InstrumentAudio audio839;
  InstrumentAudioEvent audioEvent840;
  InstrumentAudio audio841;
  InstrumentAudioEvent audioEvent842;
  InstrumentAudio audio843;
  InstrumentAudioEvent audioEvent844;
  InstrumentAudio audio845;
  InstrumentAudioEvent audioEvent846;
  InstrumentAudio audio847;
  InstrumentAudioEvent audioEvent848;
  InstrumentAudio audio849;
  InstrumentAudioEvent audioEvent850;
  InstrumentAudio audio851;
  InstrumentAudioEvent audioEvent852;
  InstrumentAudio audio853;
  InstrumentAudioEvent audioEvent854;
  InstrumentAudio audio855;
  InstrumentAudioEvent audioEvent856;
  InstrumentAudio audio857;
  InstrumentAudioEvent audioEvent858;
  InstrumentAudio audio859;
  InstrumentAudioEvent audioEvent860;
  InstrumentAudio audio861;
  InstrumentAudioEvent audioEvent862;
  InstrumentAudio audio863;
  InstrumentAudioEvent audioEvent864;
  InstrumentAudio audio865;
  InstrumentAudioEvent audioEvent866;
  InstrumentAudio audio867;
  InstrumentAudioEvent audioEvent868;
  InstrumentAudio audio869;
  InstrumentAudioEvent audioEvent870;
  InstrumentAudio audio871;
  InstrumentAudioEvent audioEvent872;
  InstrumentAudio audio873;
  InstrumentAudioEvent audioEvent874;
  InstrumentAudio audio875;
  InstrumentAudioEvent audioEvent876;
  InstrumentAudio audio877;
  InstrumentAudioEvent audioEvent878;
  InstrumentAudio audio879;
  InstrumentAudioEvent audioEvent880;
  InstrumentAudio audio881;
  InstrumentAudioEvent audioEvent882;
  InstrumentAudio audio883;
  InstrumentAudioEvent audioEvent884;
  InstrumentAudio audio885;
  InstrumentAudioEvent audioEvent886;
  InstrumentAudio audio887;
  InstrumentAudioEvent audioEvent888;
  InstrumentAudio audio889;
  InstrumentAudioEvent audioEvent890;
  InstrumentAudio audio891;
  InstrumentAudioEvent audioEvent892;
  InstrumentAudio audio893;
  InstrumentAudioEvent audioEvent894;
  InstrumentAudio audio895;
  InstrumentAudioEvent audioEvent896;
  InstrumentAudio audio897;
  InstrumentAudioEvent audioEvent898;
  InstrumentAudio audio899;
  InstrumentAudioEvent audioEvent900;
  InstrumentAudio audio901;
  InstrumentAudioEvent audioEvent902;
  InstrumentAudio audio903;
  InstrumentAudioEvent audioEvent904;
  InstrumentAudio audio905;
  InstrumentAudioEvent audioEvent906;
  InstrumentAudio audio907;
  InstrumentAudioEvent audioEvent908;
  InstrumentAudio audio909;
  InstrumentAudioEvent audioEvent910;
  Instrument instrument36;
  InstrumentAudio audio911;
  InstrumentAudioEvent audioEvent912;
  InstrumentAudio audio913;
  InstrumentAudioEvent audioEvent914;
  InstrumentAudio audio915;
  InstrumentAudioEvent audioEvent916;
  InstrumentAudio audio917;
  InstrumentAudioEvent audioEvent918;
  InstrumentAudio audio919;
  InstrumentAudioEvent audioEvent920;
  InstrumentAudio audio921;
  InstrumentAudioEvent audioEvent922;
  InstrumentAudio audio923;
  InstrumentAudioEvent audioEvent924;
  InstrumentAudio audio925;
  InstrumentAudioEvent audioEvent926;
  InstrumentAudio audio927;
  InstrumentAudioEvent audioEvent928;
  InstrumentAudio audio929;
  InstrumentAudioEvent audioEvent930;
  InstrumentAudio audio931;
  InstrumentAudioEvent audioEvent932;
  InstrumentAudio audio933;
  InstrumentAudioEvent audioEvent934;
  InstrumentAudio audio935;
  InstrumentAudioEvent audioEvent936;
  InstrumentAudio audio937;
  InstrumentAudioEvent audioEvent938;
  InstrumentAudio audio939;
  InstrumentAudioEvent audioEvent940;
  InstrumentAudio audio941;
  InstrumentAudio audio942;
  InstrumentAudioEvent audioEvent943;
  InstrumentAudio audio944;
  InstrumentAudioEvent audioEvent945;
  InstrumentAudio audio946;
  InstrumentAudioEvent audioEvent947;
  InstrumentAudio audio948;
  InstrumentAudioEvent audioEvent949;
  Instrument instrument37;
  Instrument instrument31;
  InstrumentAudio audio950;
  InstrumentAudioEvent audioEvent951;
  InstrumentAudio audio952;
  InstrumentAudioEvent audioEvent953;
  InstrumentAudio audio954;
  InstrumentAudioEvent audioEvent955;
  InstrumentAudio audio956;
  InstrumentAudioEvent audioEvent957;
  InstrumentAudio audio958;
  InstrumentAudioEvent audioEvent959;
  InstrumentAudio audio960;
  InstrumentAudioEvent audioEvent961;
  InstrumentAudio audio962;
  InstrumentAudioEvent audioEvent963;
  InstrumentAudio audio964;
  InstrumentAudioEvent audioEvent965;
  InstrumentAudio audio966;
  InstrumentAudioEvent audioEvent967;
  InstrumentAudio audio968;
  InstrumentAudioEvent audioEvent969;
  InstrumentAudio audio970;
  InstrumentAudioEvent audioEvent971;
  InstrumentAudio audio972;
  InstrumentAudioEvent audioEvent973;
  InstrumentAudio audio974;
  InstrumentAudioEvent audioEvent975;
  InstrumentAudio audio976;
  InstrumentAudioEvent audioEvent977;
  InstrumentAudio audio978;
  InstrumentAudioEvent audioEvent979;
  InstrumentAudio audio980;
  InstrumentAudioEvent audioEvent981;
  InstrumentAudio audio982;
  InstrumentAudioEvent audioEvent983;
  InstrumentAudio audio984;
  InstrumentAudioEvent audioEvent985;
  InstrumentAudio audio986;
  InstrumentAudioEvent audioEvent987;
  InstrumentAudio audio988;
  InstrumentAudioEvent audioEvent989;
  InstrumentAudio audio990;
  InstrumentAudioEvent audioEvent991;
  InstrumentAudio audio992;
  InstrumentAudioEvent audioEvent993;
  InstrumentAudio audio994;
  InstrumentAudioEvent audioEvent995;
  InstrumentAudio audio996;
  InstrumentAudioEvent audioEvent997;
  InstrumentAudio audio998;
  InstrumentAudioEvent audioEvent999;
  InstrumentAudio audio1000;
  InstrumentAudioEvent audioEvent1001;
  InstrumentAudio audio1002;
  InstrumentAudioEvent audioEvent1003;
  InstrumentAudio audio1004;
  InstrumentAudioEvent audioEvent1005;
  InstrumentAudio audio1006;
  InstrumentAudioEvent audioEvent1007;
  InstrumentAudio audio1008;
  InstrumentAudioEvent audioEvent1009;
  InstrumentAudio audio1010;
  InstrumentAudioEvent audioEvent1011;
  InstrumentAudio audio1012;
  InstrumentAudioEvent audioEvent1013;
  InstrumentAudio audio1014;
  InstrumentAudioEvent audioEvent1015;
  InstrumentAudio audio1016;
  InstrumentAudioEvent audioEvent1017;
  InstrumentAudio audio1018;
  InstrumentAudioEvent audioEvent1019;
  InstrumentAudio audio1020;
  InstrumentAudioEvent audioEvent1021;
  InstrumentAudio audio1022;
  InstrumentAudioEvent audioEvent1023;
  InstrumentAudio audio1024;
  InstrumentAudioEvent audioEvent1025;
  InstrumentAudio audio1026;
  InstrumentAudioEvent audioEvent1027;
  InstrumentAudio audio1028;
  InstrumentAudioEvent audioEvent1029;
  InstrumentAudio audio1030;
  InstrumentAudioEvent audioEvent1031;
  InstrumentAudio audio1032;
  InstrumentAudioEvent audioEvent1033;
  InstrumentAudio audio1034;
  InstrumentAudioEvent audioEvent1035;
  InstrumentAudio audio1036;
  InstrumentAudioEvent audioEvent1037;
  InstrumentAudio audio1038;
  InstrumentAudioEvent audioEvent1039;
  InstrumentAudio audio1040;
  InstrumentAudioEvent audioEvent1041;
  InstrumentAudio audio1042;
  InstrumentAudioEvent audioEvent1043;
  InstrumentAudio audio1044;
  InstrumentAudioEvent audioEvent1045;
  InstrumentAudio audio1046;
  InstrumentAudioEvent audioEvent1047;
  InstrumentAudio audio1048;
  InstrumentAudioEvent audioEvent1049;
  InstrumentAudio audio1050;
  InstrumentAudioEvent audioEvent1051;
  InstrumentAudio audio1052;
  InstrumentAudioEvent audioEvent1053;
  InstrumentAudio audio1054;
  InstrumentAudioEvent audioEvent1055;
  InstrumentAudio audio1056;
  InstrumentAudioEvent audioEvent1057;
  InstrumentAudio audio1058;
  InstrumentAudioEvent audioEvent1059;
  InstrumentAudio audio1060;
  InstrumentAudioEvent audioEvent1061;
  InstrumentAudio audio1062;
  InstrumentAudioEvent audioEvent1063;
  InstrumentAudio audio1064;
  InstrumentAudioEvent audioEvent1065;
  InstrumentAudio audio1066;
  InstrumentAudioEvent audioEvent1067;
  InstrumentAudio audio1068;
  InstrumentAudioEvent audioEvent1069;
  InstrumentAudio audio1070;
  InstrumentAudioEvent audioEvent1071;
  InstrumentAudio audio1072;
  InstrumentAudioEvent audioEvent1073;
  InstrumentAudio audio1074;
  InstrumentAudioEvent audioEvent1075;
  InstrumentAudio audio1076;
  InstrumentAudioEvent audioEvent1077;
  InstrumentAudio audio1078;
  InstrumentAudioEvent audioEvent1079;
  InstrumentAudio audio1080;
  InstrumentAudioEvent audioEvent1081;
  InstrumentAudio audio1082;
  InstrumentAudioEvent audioEvent1083;
  InstrumentAudio audio1084;
  InstrumentAudioEvent audioEvent1085;
  InstrumentAudio audio1086;
  InstrumentAudioEvent audioEvent1087;
  InstrumentAudio audio1088;
  InstrumentAudioEvent audioEvent1089;
  InstrumentAudio audio1090;
  InstrumentAudioEvent audioEvent1091;
  InstrumentAudio audio1092;
  InstrumentAudioEvent audioEvent1093;
  InstrumentAudio audio1094;
  InstrumentAudioEvent audioEvent1095;
  InstrumentAudio audio1096;
  InstrumentAudioEvent audioEvent1097;
  Instrument instrument27;
  InstrumentAudio audio1098;
  InstrumentAudioEvent audioEvent1099;
  InstrumentAudio audio1100;
  InstrumentAudioEvent audioEvent1101;
  InstrumentAudio audio1102;
  InstrumentAudioEvent audioEvent1103;
  InstrumentAudio audio1104;
  InstrumentAudioEvent audioEvent1105;
  InstrumentAudio audio1106;
  InstrumentAudioEvent audioEvent1107;
  InstrumentAudio audio1108;
  InstrumentAudioEvent audioEvent1109;
  InstrumentAudio audio1110;
  InstrumentAudioEvent audioEvent1111;
  InstrumentAudio audio1112;
  InstrumentAudioEvent audioEvent1113;
  InstrumentAudio audio1114;
  InstrumentAudioEvent audioEvent1115;
  InstrumentAudio audio1116;
  InstrumentAudioEvent audioEvent1117;
  InstrumentAudio audio1118;
  InstrumentAudioEvent audioEvent1119;
  InstrumentAudio audio1120;
  InstrumentAudioEvent audioEvent1121;
  InstrumentAudio audio1122;
  InstrumentAudioEvent audioEvent1123;
  InstrumentAudio audio1124;
  InstrumentAudioEvent audioEvent1125;
  InstrumentAudio audio1126;
  InstrumentAudioEvent audioEvent1127;
  InstrumentAudio audio1128;
  InstrumentAudioEvent audioEvent1129;
  InstrumentAudio audio1130;
  InstrumentAudioEvent audioEvent1131;
  InstrumentAudio audio1132;
  InstrumentAudioEvent audioEvent1133;
  InstrumentAudio audio1134;
  InstrumentAudioEvent audioEvent1135;
  Instrument instrument38;
  InstrumentAudio audio1136;
  InstrumentAudioEvent audioEvent1137;
  InstrumentAudio audio1138;
  InstrumentAudioEvent audioEvent1139;
  InstrumentAudio audio1140;
  InstrumentAudioEvent audioEvent1141;
  InstrumentAudio audio1142;
  InstrumentAudioEvent audioEvent1143;
  InstrumentAudio audio1144;
  InstrumentAudioEvent audioEvent1145;
  InstrumentAudio audio1146;
  InstrumentAudioEvent audioEvent1147;
  InstrumentAudio audio1148;
  InstrumentAudioEvent audioEvent1149;
  InstrumentAudio audio1150;
  InstrumentAudioEvent audioEvent1151;
  InstrumentAudio audio1152;
  InstrumentAudioEvent audioEvent1153;
  InstrumentAudio audio1154;
  InstrumentAudioEvent audioEvent1155;
  InstrumentAudio audio1156;
  InstrumentAudioEvent audioEvent1157;
  InstrumentAudio audio1158;
  InstrumentAudioEvent audioEvent1159;
  InstrumentAudio audio1160;
  InstrumentAudioEvent audioEvent1161;
  InstrumentAudio audio1162;
  InstrumentAudioEvent audioEvent1163;
  InstrumentAudio audio1164;
  InstrumentAudioEvent audioEvent1165;
  InstrumentAudio audio1166;
  InstrumentAudioEvent audioEvent1167;
  InstrumentAudio audio1168;
  InstrumentAudioEvent audioEvent1169;
  InstrumentAudio audio1170;
  InstrumentAudioEvent audioEvent1171;
  InstrumentAudio audio1172;
  InstrumentAudioEvent audioEvent1173;
  InstrumentAudio audio1174;
  InstrumentAudioEvent audioEvent1175;
  InstrumentAudio audio1176;
  InstrumentAudioEvent audioEvent1177;
  InstrumentAudio audio1178;
  InstrumentAudioEvent audioEvent1179;
  InstrumentAudio audio1180;
  InstrumentAudioEvent audioEvent1181;
  InstrumentAudio audio1182;
  InstrumentAudioEvent audioEvent1183;
  Instrument instrument39;
  InstrumentAudio audio1184;
  InstrumentAudioEvent audioEvent1185;
  InstrumentAudio audio1186;
  InstrumentAudioEvent audioEvent1187;
  InstrumentAudio audio1188;
  InstrumentAudioEvent audioEvent1189;
  InstrumentAudio audio1190;
  InstrumentAudioEvent audioEvent1191;
  InstrumentAudio audio1192;
  InstrumentAudioEvent audioEvent1193;
  InstrumentAudio audio1194;
  InstrumentAudioEvent audioEvent1195;
  InstrumentAudio audio1196;
  InstrumentAudioEvent audioEvent1197;
  InstrumentAudio audio1198;
  InstrumentAudioEvent audioEvent1199;
  InstrumentAudio audio1200;
  InstrumentAudioEvent audioEvent1201;
  InstrumentAudio audio1202;
  InstrumentAudioEvent audioEvent1203;
  InstrumentAudio audio1204;
  InstrumentAudioEvent audioEvent1205;
  InstrumentAudio audio1206;
  InstrumentAudioEvent audioEvent1207;
  InstrumentAudio audio1208;
  InstrumentAudioEvent audioEvent1209;
  InstrumentAudio audio1210;
  InstrumentAudioEvent audioEvent1211;
  InstrumentAudio audio1212;
  InstrumentAudioEvent audioEvent1213;
  InstrumentAudio audio1214;
  InstrumentAudioEvent audioEvent1215;
  InstrumentAudio audio1216;
  InstrumentAudioEvent audioEvent1217;
  InstrumentAudio audio1218;
  InstrumentAudioEvent audioEvent1219;
  InstrumentAudio audio1220;
  InstrumentAudioEvent audioEvent1221;
  InstrumentAudio audio1222;
  InstrumentAudioEvent audioEvent1223;
  InstrumentAudio audio1224;
  InstrumentAudioEvent audioEvent1225;
  InstrumentAudio audio1226;
  InstrumentAudioEvent audioEvent1227;
  Instrument instrument40;
  InstrumentAudio audio1228;
  InstrumentAudioEvent audioEvent1229;
  InstrumentAudio audio1230;
  InstrumentAudioEvent audioEvent1231;
  InstrumentAudio audio1232;
  InstrumentAudioEvent audioEvent1233;
  InstrumentAudio audio1234;
  InstrumentAudioEvent audioEvent1235;
  InstrumentAudio audio1236;
  InstrumentAudioEvent audioEvent1237;
  InstrumentAudio audio1238;
  InstrumentAudioEvent audioEvent1239;
  InstrumentAudio audio1240;
  InstrumentAudioEvent audioEvent1241;
  InstrumentAudio audio1242;
  InstrumentAudioEvent audioEvent1243;
  InstrumentAudio audio1244;
  InstrumentAudioEvent audioEvent1245;
  InstrumentAudio audio1246;
  InstrumentAudioEvent audioEvent1247;
  InstrumentAudio audio1248;
  InstrumentAudioEvent audioEvent1249;
  InstrumentAudio audio1250;
  InstrumentAudioEvent audioEvent1251;
  InstrumentAudio audio1252;
  InstrumentAudioEvent audioEvent1253;
  InstrumentAudio audio1254;
  InstrumentAudioEvent audioEvent1255;
  InstrumentAudio audio1256;
  InstrumentAudioEvent audioEvent1257;
  InstrumentAudio audio1258;
  InstrumentAudioEvent audioEvent1259;
  InstrumentAudio audio1260;
  InstrumentAudioEvent audioEvent1261;
  InstrumentAudio audio1262;
  InstrumentAudioEvent audioEvent1263;
  InstrumentAudio audio1264;
  InstrumentAudioEvent audioEvent1265;
  InstrumentAudio audio1266;
  InstrumentAudioEvent audioEvent1267;
  InstrumentAudio audio1268;
  InstrumentAudioEvent audioEvent1269;
  InstrumentAudio audio1270;
  InstrumentAudioEvent audioEvent1271;
  InstrumentAudio audio1272;
  InstrumentAudioEvent audioEvent1273;
  InstrumentAudio audio1274;
  InstrumentAudioEvent audioEvent1275;
  InstrumentAudio audio1276;
  InstrumentAudioEvent audioEvent1277;
  InstrumentAudio audio1278;
  InstrumentAudioEvent audioEvent1279;
  InstrumentAudio audio1280;
  InstrumentAudioEvent audioEvent1281;
  InstrumentAudio audio1282;
  InstrumentAudioEvent audioEvent1283;
  InstrumentAudio audio1284;
  InstrumentAudioEvent audioEvent1285;
  InstrumentAudio audio1286;
  InstrumentAudioEvent audioEvent1287;
  InstrumentAudio audio1288;
  InstrumentAudioEvent audioEvent1289;
  InstrumentAudio audio1290;
  InstrumentAudioEvent audioEvent1291;
  InstrumentAudio audio1292;
  InstrumentAudioEvent audioEvent1293;
  InstrumentAudio audio1294;
  InstrumentAudioEvent audioEvent1295;
  InstrumentAudio audio1296;
  InstrumentAudioEvent audioEvent1297;
  InstrumentAudio audio1298;
  InstrumentAudioEvent audioEvent1299;
  InstrumentAudio audio1300;
  InstrumentAudioEvent audioEvent1301;
  InstrumentAudio audio1302;
  InstrumentAudioEvent audioEvent1303;
  Instrument instrument30;
  InstrumentAudio audio1304;
  InstrumentAudioEvent audioEvent1305;
  InstrumentAudio audio1306;
  InstrumentAudioEvent audioEvent1307;
  InstrumentAudio audio1308;
  InstrumentAudioEvent audioEvent1309;
  InstrumentAudio audio1310;
  InstrumentAudioEvent audioEvent1311;
  InstrumentAudio audio1312;
  InstrumentAudioEvent audioEvent1313;
  InstrumentAudio audio1314;
  InstrumentAudioEvent audioEvent1315;
  InstrumentAudio audio1316;
  InstrumentAudioEvent audioEvent1317;
  InstrumentAudio audio1318;
  InstrumentAudioEvent audioEvent1319;
  InstrumentAudio audio1320;
  InstrumentAudioEvent audioEvent1321;
  InstrumentAudio audio1322;
  InstrumentAudioEvent audioEvent1323;
  InstrumentAudio audio1324;
  InstrumentAudioEvent audioEvent1325;
  InstrumentAudio audio1326;
  InstrumentAudioEvent audioEvent1327;
  InstrumentAudio audio1328;
  InstrumentAudioEvent audioEvent1329;
  InstrumentAudio audio1330;
  InstrumentAudioEvent audioEvent1331;
  InstrumentAudio audio1332;
  InstrumentAudioEvent audioEvent1333;
  InstrumentAudio audio1334;
  InstrumentAudioEvent audioEvent1335;
  InstrumentAudio audio1336;
  InstrumentAudioEvent audioEvent1337;
  InstrumentAudio audio1338;
  InstrumentAudioEvent audioEvent1339;
  InstrumentAudio audio1340;
  InstrumentAudioEvent audioEvent1341;
  InstrumentAudio audio1342;
  InstrumentAudioEvent audioEvent1343;
  InstrumentAudio audio1344;
  InstrumentAudioEvent audioEvent1345;
  InstrumentAudio audio1346;
  InstrumentAudioEvent audioEvent1347;
  InstrumentAudio audio1348;
  InstrumentAudioEvent audioEvent1349;
  InstrumentAudio audio1350;
  InstrumentAudioEvent audioEvent1351;
  Program program8;
  ProgramSequence sequence1352;
  ProgramSequence sequence1353;
  ProgramSequenceBinding sequenceBinding1354;
  ProgramSequenceBinding sequenceBinding1355;
  ProgramSequenceBindingMeme sequenceBindingMeme1356;
  ProgramSequenceBindingMeme sequenceBindingMeme1357;
  ProgramSequenceBindingMeme sequenceBindingMeme1358;
  ProgramSequenceBindingMeme sequenceBindingMeme1359;
  Program program7;
  ProgramSequence sequence1360;
  ProgramSequence sequence1361;
  ProgramSequenceBinding sequenceBinding1362;
  ProgramSequenceBinding sequenceBinding1363;
  ProgramSequenceBindingMeme sequenceBindingMeme1364;
  ProgramSequenceBindingMeme sequenceBindingMeme1365;
  ProgramSequenceBindingMeme sequenceBindingMeme1366;
  ProgramSequenceBindingMeme sequenceBindingMeme1367;
  Program program12;
  ProgramSequence sequence1368;
  ProgramSequence sequence1369;
  ProgramSequenceBinding sequenceBinding1370;
  ProgramSequenceBinding sequenceBinding1371;
  ProgramSequenceBinding sequenceBinding1372;
  ProgramSequenceBindingMeme sequenceBindingMeme1373;
  ProgramSequenceBindingMeme sequenceBindingMeme1374;
  ProgramSequenceBindingMeme sequenceBindingMeme1375;
  Program program13;
  ProgramSequence sequence1376;
  ProgramSequence sequence1377;
  ProgramSequenceBinding sequenceBinding1378;
  ProgramSequenceBinding sequenceBinding1379;
  ProgramSequenceBindingMeme sequenceBindingMeme1380;
  ProgramSequenceBindingMeme sequenceBindingMeme1381;
  Program program14;
  ProgramSequence sequence1382;
  ProgramSequence sequence1383;
  ProgramSequenceBinding sequenceBinding1384;
  ProgramSequenceBinding sequenceBinding1385;
  ProgramSequenceBindingMeme sequenceBindingMeme1386;
  ProgramSequenceBindingMeme sequenceBindingMeme1387;
  Program program15;
  ProgramSequence sequence1388;
  ProgramSequence sequence1389;
  ProgramSequenceBinding sequenceBinding1390;
  ProgramSequenceBinding sequenceBinding1391;
  ProgramSequenceBindingMeme sequenceBindingMeme1392;
  ProgramSequenceBindingMeme sequenceBindingMeme1393;
  Program program16;
  ProgramSequence sequence1394;
  ProgramSequence sequence1395;
  ProgramSequence sequence1396;
  ProgramSequenceBinding sequenceBinding1397;
  ProgramSequenceBinding sequenceBinding1398;
  ProgramSequenceBinding sequenceBinding1399;
  ProgramSequenceBindingMeme sequenceBindingMeme1400;
  ProgramSequenceBindingMeme sequenceBindingMeme1401;
  ProgramSequenceBindingMeme sequenceBindingMeme1402;
  ProgramSequenceBindingMeme sequenceBindingMeme1403;
  Program program17;
  ProgramSequence sequence1404;
  ProgramSequence sequence1405;
  ProgramSequence sequence1406;
  ProgramSequenceBinding sequenceBinding1407;
  ProgramSequenceBinding sequenceBinding1408;
  ProgramSequenceBinding sequenceBinding1409;
  ProgramSequenceBindingMeme sequenceBindingMeme1410;
  ProgramSequenceBindingMeme sequenceBindingMeme1411;
  ProgramSequenceBindingMeme sequenceBindingMeme1412;
  ProgramSequenceBindingMeme sequenceBindingMeme1413;
  Program program32;
  ProgramSequence sequence1414;
  ProgramSequence sequence1415;
  ProgramSequence sequence1416;
  ProgramSequenceBinding sequenceBinding1417;
  ProgramSequenceBinding sequenceBinding1418;
  ProgramSequenceBinding sequenceBinding1419;
  ProgramSequenceBindingMeme sequenceBindingMeme1420;
  ProgramSequenceBindingMeme sequenceBindingMeme1421;
  ProgramSequenceBindingMeme sequenceBindingMeme1422;
  ProgramSequenceBindingMeme sequenceBindingMeme1423;
  Program program31;
  ProgramSequence sequence1424;
  ProgramSequence sequence1425;
  ProgramSequence sequence1426;
  ProgramSequenceBinding sequenceBinding1427;
  ProgramSequenceBinding sequenceBinding1428;
  ProgramSequenceBinding sequenceBinding1429;
  ProgramSequenceBindingMeme sequenceBindingMeme1430;
  ProgramSequenceBindingMeme sequenceBindingMeme1431;
  ProgramSequenceBindingMeme sequenceBindingMeme1432;
  ProgramSequenceBindingMeme sequenceBindingMeme1433;
  Program program30;
  ProgramSequence sequence1434;
  ProgramSequence sequence1435;
  ProgramSequence sequence1436;
  ProgramSequenceBinding sequenceBinding1437;
  ProgramSequenceBinding sequenceBinding1438;
  ProgramSequenceBinding sequenceBinding1439;
  ProgramSequenceBindingMeme sequenceBindingMeme1440;
  ProgramSequenceBindingMeme sequenceBindingMeme1441;
  ProgramSequenceBindingMeme sequenceBindingMeme1442;
  ProgramSequenceBindingMeme sequenceBindingMeme1443;
  Program program18;
  ProgramSequence sequence1444;
  ProgramSequence sequence1445;
  ProgramSequence sequence1446;
  ProgramSequenceBinding sequenceBinding1447;
  ProgramSequenceBinding sequenceBinding1448;
  ProgramSequenceBinding sequenceBinding1449;
  ProgramSequenceBindingMeme sequenceBindingMeme1450;
  ProgramSequenceBindingMeme sequenceBindingMeme1451;
  ProgramSequenceBindingMeme sequenceBindingMeme1452;
  ProgramSequenceBindingMeme sequenceBindingMeme1453;
  Program program19;
  ProgramSequence sequence1454;
  ProgramSequence sequence1455;
  ProgramSequenceBinding sequenceBinding1456;
  ProgramSequenceBinding sequenceBinding1457;
  ProgramSequenceBindingMeme sequenceBindingMeme1458;
  ProgramSequenceBindingMeme sequenceBindingMeme1459;
  Program program20;
  ProgramSequence sequence1460;
  ProgramSequence sequence1461;
  ProgramSequenceBinding sequenceBinding1462;
  ProgramSequenceBinding sequenceBinding1463;
  ProgramSequenceBindingMeme sequenceBindingMeme1464;
  ProgramSequenceBindingMeme sequenceBindingMeme1465;
  Program program81;
  ProgramSequence sequence1466;
  ProgramSequence sequence1467;
  ProgramSequence sequence1468;
  ProgramSequenceChord sequenceChord1469;
  ProgramSequenceChord sequenceChord1470;
  ProgramSequenceChord sequenceChord1471;
  ProgramSequenceChord sequenceChord1472;
  ProgramSequenceChord sequenceChord1473;
  ProgramSequenceChord sequenceChord1474;
  ProgramSequenceChord sequenceChord1475;
  ProgramSequenceChord sequenceChord1476;
  ProgramSequenceChord sequenceChord1477;
  ProgramSequenceChord sequenceChord1478;
  ProgramSequenceChord sequenceChord1479;
  ProgramSequenceChord sequenceChord1480;
  ProgramSequenceChord sequenceChord1481;
  ProgramSequenceChord sequenceChord1482;
  ProgramSequenceBinding sequenceBinding1483;
  ProgramSequenceBinding sequenceBinding1484;
  ProgramSequenceBinding sequenceBinding1485;
  ProgramSequenceBinding sequenceBinding1486;
  ProgramSequenceBinding sequenceBinding1487;
  ProgramSequenceBinding sequenceBinding1488;
  ProgramSequenceBinding sequenceBinding1489;
  ProgramSequenceBinding sequenceBinding1490;
  ProgramSequenceBinding sequenceBinding1491;
  ProgramSequenceBinding sequenceBinding1492;
  ProgramSequenceBinding sequenceBinding1493;
  ProgramSequenceBinding sequenceBinding1494;
  ProgramSequenceBinding sequenceBinding1495;
  ProgramSequenceBinding sequenceBinding1496;
  ProgramSequenceBinding sequenceBinding1497;
  ProgramSequenceBinding sequenceBinding1498;
  ProgramSequenceBinding sequenceBinding1499;
  ProgramSequenceBinding sequenceBinding1500;
  ProgramSequenceBinding sequenceBinding1501;
  ProgramSequenceBinding sequenceBinding1502;
  ProgramSequenceBinding sequenceBinding1503;
  ProgramSequenceBindingMeme sequenceBindingMeme1504;
  ProgramSequenceBindingMeme sequenceBindingMeme1505;
  ProgramSequenceBindingMeme sequenceBindingMeme1506;
  ProgramSequenceBindingMeme sequenceBindingMeme1507;
  ProgramSequenceBindingMeme sequenceBindingMeme1508;
  ProgramSequenceBindingMeme sequenceBindingMeme1509;
  ProgramSequenceBindingMeme sequenceBindingMeme1510;
  ProgramSequenceBindingMeme sequenceBindingMeme1511;
  ProgramSequenceBindingMeme sequenceBindingMeme1512;
  ProgramSequenceBindingMeme sequenceBindingMeme1513;
  ProgramSequenceBindingMeme sequenceBindingMeme1514;
  ProgramSequenceBindingMeme sequenceBindingMeme1515;
  ProgramSequenceBindingMeme sequenceBindingMeme1516;
  ProgramSequenceBindingMeme sequenceBindingMeme1517;
  ProgramSequenceBindingMeme sequenceBindingMeme1518;
  ProgramSequenceBindingMeme sequenceBindingMeme1519;
  ProgramSequenceBindingMeme sequenceBindingMeme1520;
  ProgramSequenceBindingMeme sequenceBindingMeme1521;
  ProgramSequenceBindingMeme sequenceBindingMeme1522;
  ProgramSequenceBindingMeme sequenceBindingMeme1523;
  ProgramSequenceBindingMeme sequenceBindingMeme1524;
  Program program61;
  ProgramSequence sequence1525;
  ProgramSequenceChord sequenceChord1526;
  ProgramSequenceChord sequenceChord1527;
  ProgramSequenceChord sequenceChord1528;
  ProgramSequenceChord sequenceChord1529;
  ProgramSequenceChord sequenceChord1530;
  ProgramSequenceChord sequenceChord1531;
  ProgramSequenceChord sequenceChord1532;
  ProgramSequenceChord sequenceChord1533;
  ProgramSequenceChord sequenceChord1534;
  ProgramSequenceBinding sequenceBinding1535;
  ProgramSequenceBinding sequenceBinding1536;
  ProgramSequenceBinding sequenceBinding1537;
  ProgramSequenceBinding sequenceBinding1538;
  ProgramSequenceBinding sequenceBinding1539;
  ProgramSequenceBinding sequenceBinding1540;
  ProgramSequenceBinding sequenceBinding1541;
  ProgramSequenceBinding sequenceBinding1542;
  ProgramSequenceBinding sequenceBinding1543;
  ProgramSequenceBinding sequenceBinding1544;
  ProgramSequenceBinding sequenceBinding1545;
  ProgramSequenceBinding sequenceBinding1546;
  ProgramSequenceBinding sequenceBinding1547;
  ProgramSequenceBinding sequenceBinding1548;
  ProgramSequenceBinding sequenceBinding1549;
  ProgramSequenceBinding sequenceBinding1550;
  ProgramSequenceBinding sequenceBinding1551;
  ProgramSequenceBindingMeme sequenceBindingMeme1552;
  ProgramSequenceBindingMeme sequenceBindingMeme1553;
  ProgramSequenceBindingMeme sequenceBindingMeme1554;
  ProgramSequenceBindingMeme sequenceBindingMeme1555;
  ProgramSequenceBindingMeme sequenceBindingMeme1556;
  ProgramSequenceBindingMeme sequenceBindingMeme1557;
  ProgramSequenceBindingMeme sequenceBindingMeme1558;
  ProgramSequenceBindingMeme sequenceBindingMeme1559;
  ProgramSequenceBindingMeme sequenceBindingMeme1560;
  ProgramSequenceBindingMeme sequenceBindingMeme1561;
  ProgramSequenceBindingMeme sequenceBindingMeme1562;
  ProgramSequenceBindingMeme sequenceBindingMeme1563;
  ProgramSequenceBindingMeme sequenceBindingMeme1564;
  ProgramSequenceBindingMeme sequenceBindingMeme1565;
  ProgramSequenceBindingMeme sequenceBindingMeme1566;
  ProgramSequenceBindingMeme sequenceBindingMeme1567;
  ProgramSequenceBindingMeme sequenceBindingMeme1568;
  ProgramSequenceBindingMeme sequenceBindingMeme1569;
  ProgramSequenceBindingMeme sequenceBindingMeme1570;
  ProgramSequenceBindingMeme sequenceBindingMeme1571;
  ProgramSequenceBindingMeme sequenceBindingMeme1572;
  ProgramSequenceBindingMeme sequenceBindingMeme1573;
  ProgramSequenceBindingMeme sequenceBindingMeme1574;
  ProgramSequenceBindingMeme sequenceBindingMeme1575;
  Program program47;
  ProgramSequence sequence1576;
  ProgramSequence sequence1577;
  ProgramSequence sequence1578;
  ProgramSequence sequence1579;
  ProgramSequenceChord sequenceChord1580;
  ProgramSequenceChord sequenceChord1581;
  ProgramSequenceChord sequenceChord1582;
  ProgramSequenceChord sequenceChord1583;
  ProgramSequenceChord sequenceChord1584;
  ProgramSequenceChord sequenceChord1585;
  ProgramSequenceChord sequenceChord1586;
  ProgramSequenceChord sequenceChord1587;
  ProgramSequenceChord sequenceChord1588;
  ProgramSequenceChord sequenceChord1589;
  ProgramSequenceChord sequenceChord1590;
  ProgramSequenceChord sequenceChord1591;
  ProgramSequenceChord sequenceChord1592;
  ProgramSequenceChord sequenceChord1593;
  ProgramSequenceChord sequenceChord1594;
  ProgramSequenceChord sequenceChord1595;
  ProgramSequenceChord sequenceChord1596;
  ProgramSequenceBinding sequenceBinding1597;
  ProgramSequenceBinding sequenceBinding1598;
  ProgramSequenceBinding sequenceBinding1599;
  ProgramSequenceBinding sequenceBinding1600;
  ProgramSequenceBinding sequenceBinding1601;
  ProgramSequenceBinding sequenceBinding1602;
  ProgramSequenceBinding sequenceBinding1603;
  ProgramSequenceBinding sequenceBinding1604;
  ProgramSequenceBinding sequenceBinding1605;
  ProgramSequenceBinding sequenceBinding1606;
  ProgramSequenceBinding sequenceBinding1607;
  ProgramSequenceBinding sequenceBinding1608;
  ProgramSequenceBinding sequenceBinding1609;
  ProgramSequenceBinding sequenceBinding1610;
  ProgramSequenceBinding sequenceBinding1611;
  ProgramSequenceBinding sequenceBinding1612;
  ProgramSequenceBinding sequenceBinding1613;
  ProgramSequenceBinding sequenceBinding1614;
  ProgramSequenceBinding sequenceBinding1615;
  ProgramSequenceBinding sequenceBinding1616;
  ProgramSequenceBinding sequenceBinding1617;
  ProgramSequenceBinding sequenceBinding1618;
  ProgramSequenceBinding sequenceBinding1619;
  ProgramSequenceBinding sequenceBinding1620;
  ProgramSequenceBinding sequenceBinding1621;
  ProgramSequenceBinding sequenceBinding1622;
  ProgramSequenceBindingMeme sequenceBindingMeme1623;
  ProgramSequenceBindingMeme sequenceBindingMeme1624;
  ProgramSequenceBindingMeme sequenceBindingMeme1625;
  ProgramSequenceBindingMeme sequenceBindingMeme1626;
  ProgramSequenceBindingMeme sequenceBindingMeme1627;
  ProgramSequenceBindingMeme sequenceBindingMeme1628;
  ProgramSequenceBindingMeme sequenceBindingMeme1629;
  ProgramSequenceBindingMeme sequenceBindingMeme1630;
  ProgramSequenceBindingMeme sequenceBindingMeme1631;
  ProgramSequenceBindingMeme sequenceBindingMeme1632;
  ProgramSequenceBindingMeme sequenceBindingMeme1633;
  ProgramSequenceBindingMeme sequenceBindingMeme1634;
  ProgramSequenceBindingMeme sequenceBindingMeme1635;
  ProgramSequenceBindingMeme sequenceBindingMeme1636;
  ProgramSequenceBindingMeme sequenceBindingMeme1637;
  ProgramSequenceBindingMeme sequenceBindingMeme1638;
  ProgramSequenceBindingMeme sequenceBindingMeme1639;
  ProgramSequenceBindingMeme sequenceBindingMeme1640;
  ProgramSequenceBindingMeme sequenceBindingMeme1641;
  ProgramSequenceBindingMeme sequenceBindingMeme1642;
  ProgramSequenceBindingMeme sequenceBindingMeme1643;
  ProgramSequenceBindingMeme sequenceBindingMeme1644;
  ProgramSequenceBindingMeme sequenceBindingMeme1645;
  ProgramSequenceBindingMeme sequenceBindingMeme1646;
  ProgramSequenceBindingMeme sequenceBindingMeme1647;
  ProgramSequenceBindingMeme sequenceBindingMeme1648;
  ProgramSequenceBindingMeme sequenceBindingMeme1649;
  ProgramSequenceBindingMeme sequenceBindingMeme1650;
  ProgramSequenceBindingMeme sequenceBindingMeme1651;
  ProgramSequenceBindingMeme sequenceBindingMeme1652;
  ProgramSequenceBindingMeme sequenceBindingMeme1653;
  ProgramSequenceBindingMeme sequenceBindingMeme1654;
  ProgramSequenceBindingMeme sequenceBindingMeme1655;
  ProgramSequenceBindingMeme sequenceBindingMeme1656;
  ProgramSequenceBindingMeme sequenceBindingMeme1657;
  ProgramSequenceBindingMeme sequenceBindingMeme1658;
  ProgramSequenceBindingMeme sequenceBindingMeme1659;
  ProgramSequenceBindingMeme sequenceBindingMeme1660;
  ProgramSequenceBindingMeme sequenceBindingMeme1661;
  ProgramSequenceBindingMeme sequenceBindingMeme1662;
  Program program66;
  ProgramSequence sequence1663;
  ProgramSequence sequence1664;
  ProgramSequence sequence1665;
  ProgramSequenceChord sequenceChord1666;
  ProgramSequenceChord sequenceChord1667;
  ProgramSequenceChord sequenceChord1668;
  ProgramSequenceChord sequenceChord1669;
  ProgramSequenceChord sequenceChord1670;
  ProgramSequenceChord sequenceChord1671;
  ProgramSequenceChord sequenceChord1672;
  ProgramSequenceChord sequenceChord1673;
  ProgramSequenceChord sequenceChord1674;
  ProgramSequenceChord sequenceChord1675;
  ProgramSequenceChord sequenceChord1676;
  ProgramSequenceChord sequenceChord1677;
  ProgramSequenceBinding sequenceBinding1678;
  ProgramSequenceBinding sequenceBinding1679;
  ProgramSequenceBinding sequenceBinding1680;
  ProgramSequenceBinding sequenceBinding1681;
  ProgramSequenceBinding sequenceBinding1682;
  ProgramSequenceBinding sequenceBinding1683;
  ProgramSequenceBinding sequenceBinding1684;
  ProgramSequenceBinding sequenceBinding1685;
  ProgramSequenceBinding sequenceBinding1686;
  ProgramSequenceBinding sequenceBinding1687;
  ProgramSequenceBinding sequenceBinding1688;
  ProgramSequenceBinding sequenceBinding1689;
  ProgramSequenceBinding sequenceBinding1690;
  ProgramSequenceBindingMeme sequenceBindingMeme1691;
  ProgramSequenceBindingMeme sequenceBindingMeme1692;
  ProgramSequenceBindingMeme sequenceBindingMeme1693;
  ProgramSequenceBindingMeme sequenceBindingMeme1694;
  ProgramSequenceBindingMeme sequenceBindingMeme1695;
  ProgramSequenceBindingMeme sequenceBindingMeme1696;
  ProgramSequenceBindingMeme sequenceBindingMeme1697;
  ProgramSequenceBindingMeme sequenceBindingMeme1698;
  ProgramSequenceBindingMeme sequenceBindingMeme1699;
  ProgramSequenceBindingMeme sequenceBindingMeme1700;
  ProgramSequenceBindingMeme sequenceBindingMeme1701;
  ProgramSequenceBindingMeme sequenceBindingMeme1702;
  ProgramSequenceBindingMeme sequenceBindingMeme1703;
  Program program79;
  ProgramSequence sequence1704;
  ProgramSequence sequence1705;
  ProgramSequenceChord sequenceChord1706;
  ProgramSequenceChord sequenceChord1707;
  ProgramSequenceChord sequenceChord1708;
  ProgramSequenceChord sequenceChord1709;
  ProgramSequenceChord sequenceChord1710;
  ProgramSequenceChord sequenceChord1711;
  ProgramSequenceChord sequenceChord1712;
  ProgramSequenceChord sequenceChord1713;
  ProgramSequenceChord sequenceChord1714;
  ProgramSequenceChord sequenceChord1715;
  ProgramSequenceChord sequenceChord1716;
  ProgramSequenceChord sequenceChord1717;
  ProgramSequenceBinding sequenceBinding1718;
  ProgramSequenceBinding sequenceBinding1719;
  ProgramSequenceBinding sequenceBinding1720;
  ProgramSequenceBinding sequenceBinding1721;
  ProgramSequenceBinding sequenceBinding1722;
  ProgramSequenceBinding sequenceBinding1723;
  ProgramSequenceBinding sequenceBinding1724;
  ProgramSequenceBinding sequenceBinding1725;
  ProgramSequenceBinding sequenceBinding1726;
  ProgramSequenceBinding sequenceBinding1727;
  ProgramSequenceBinding sequenceBinding1728;
  ProgramSequenceBinding sequenceBinding1729;
  ProgramSequenceBinding sequenceBinding1730;
  ProgramSequenceBinding sequenceBinding1731;
  ProgramSequenceBinding sequenceBinding1732;
  ProgramSequenceBindingMeme sequenceBindingMeme1733;
  ProgramSequenceBindingMeme sequenceBindingMeme1734;
  ProgramSequenceBindingMeme sequenceBindingMeme1735;
  ProgramSequenceBindingMeme sequenceBindingMeme1736;
  ProgramSequenceBindingMeme sequenceBindingMeme1737;
  ProgramSequenceBindingMeme sequenceBindingMeme1738;
  ProgramSequenceBindingMeme sequenceBindingMeme1739;
  ProgramSequenceBindingMeme sequenceBindingMeme1740;
  ProgramSequenceBindingMeme sequenceBindingMeme1741;
  ProgramSequenceBindingMeme sequenceBindingMeme1742;
  ProgramSequenceBindingMeme sequenceBindingMeme1743;
  ProgramSequenceBindingMeme sequenceBindingMeme1744;
  ProgramSequenceBindingMeme sequenceBindingMeme1745;
  ProgramSequenceBindingMeme sequenceBindingMeme1746;
  ProgramSequenceBindingMeme sequenceBindingMeme1747;
  Program program52;
  ProgramSequence sequence1748;
  ProgramSequence sequence1749;
  ProgramSequence sequence1750;
  ProgramSequence sequence1751;
  ProgramSequenceChord sequenceChord1752;
  ProgramSequenceChord sequenceChord1753;
  ProgramSequenceChord sequenceChord1754;
  ProgramSequenceChord sequenceChord1755;
  ProgramSequenceChord sequenceChord1756;
  ProgramSequenceChord sequenceChord1757;
  ProgramSequenceChord sequenceChord1758;
  ProgramSequenceChord sequenceChord1759;
  ProgramSequenceChord sequenceChord1760;
  ProgramSequenceChord sequenceChord1761;
  ProgramSequenceChord sequenceChord1762;
  ProgramSequenceChord sequenceChord1763;
  ProgramSequenceChord sequenceChord1764;
  ProgramSequenceChord sequenceChord1765;
  ProgramSequenceChord sequenceChord1766;
  ProgramSequenceBinding sequenceBinding1767;
  ProgramSequenceBinding sequenceBinding1768;
  ProgramSequenceBinding sequenceBinding1769;
  ProgramSequenceBinding sequenceBinding1770;
  ProgramSequenceBinding sequenceBinding1771;
  ProgramSequenceBinding sequenceBinding1772;
  ProgramSequenceBinding sequenceBinding1773;
  ProgramSequenceBinding sequenceBinding1774;
  ProgramSequenceBinding sequenceBinding1775;
  ProgramSequenceBinding sequenceBinding1776;
  ProgramSequenceBinding sequenceBinding1777;
  ProgramSequenceBinding sequenceBinding1778;
  ProgramSequenceBinding sequenceBinding1779;
  ProgramSequenceBinding sequenceBinding1780;
  ProgramSequenceBinding sequenceBinding1781;
  ProgramSequenceBindingMeme sequenceBindingMeme1782;
  ProgramSequenceBindingMeme sequenceBindingMeme1783;
  ProgramSequenceBindingMeme sequenceBindingMeme1784;
  ProgramSequenceBindingMeme sequenceBindingMeme1785;
  ProgramSequenceBindingMeme sequenceBindingMeme1786;
  ProgramSequenceBindingMeme sequenceBindingMeme1787;
  ProgramSequenceBindingMeme sequenceBindingMeme1788;
  ProgramSequenceBindingMeme sequenceBindingMeme1789;
  ProgramSequenceBindingMeme sequenceBindingMeme1790;
  ProgramSequenceBindingMeme sequenceBindingMeme1791;
  ProgramSequenceBindingMeme sequenceBindingMeme1792;
  ProgramSequenceBindingMeme sequenceBindingMeme1793;
  ProgramSequenceBindingMeme sequenceBindingMeme1794;
  ProgramSequenceBindingMeme sequenceBindingMeme1795;
  ProgramSequenceBindingMeme sequenceBindingMeme1796;
  Program program88;
  ProgramSequence sequence1797;
  ProgramSequence sequence1798;
  ProgramSequenceChord sequenceChord1799;
  ProgramSequenceChord sequenceChord1800;
  ProgramSequenceChord sequenceChord1801;
  ProgramSequenceChord sequenceChord1802;
  ProgramSequenceChord sequenceChord1803;
  ProgramSequenceChord sequenceChord1804;
  ProgramSequenceChord sequenceChord1805;
  ProgramSequenceChord sequenceChord1806;
  ProgramSequenceChord sequenceChord1807;
  ProgramSequenceChord sequenceChord1808;
  ProgramSequenceChord sequenceChord1809;
  ProgramSequenceChord sequenceChord1810;
  ProgramSequenceChord sequenceChord1811;
  ProgramSequenceChord sequenceChord1812;
  ProgramSequenceChord sequenceChord1813;
  ProgramSequenceChord sequenceChord1814;
  ProgramSequenceChord sequenceChord1815;
  ProgramSequenceChord sequenceChord1816;
  ProgramSequenceChord sequenceChord1817;
  ProgramSequenceChord sequenceChord1818;
  ProgramSequenceChord sequenceChord1819;
  ProgramSequenceChord sequenceChord1820;
  ProgramSequenceChord sequenceChord1821;
  ProgramSequenceBinding sequenceBinding1822;
  ProgramSequenceBinding sequenceBinding1823;
  ProgramSequenceBinding sequenceBinding1824;
  ProgramSequenceBinding sequenceBinding1825;
  ProgramSequenceBinding sequenceBinding1826;
  ProgramSequenceBinding sequenceBinding1827;
  ProgramSequenceBinding sequenceBinding1828;
  ProgramSequenceBinding sequenceBinding1829;
  ProgramSequenceBinding sequenceBinding1830;
  ProgramSequenceBinding sequenceBinding1831;
  ProgramSequenceBinding sequenceBinding1832;
  ProgramSequenceBinding sequenceBinding1833;
  ProgramSequenceBinding sequenceBinding1834;
  ProgramSequenceBinding sequenceBinding1835;
  ProgramSequenceBinding sequenceBinding1836;
  ProgramSequenceBinding sequenceBinding1837;
  ProgramSequenceBindingMeme sequenceBindingMeme1838;
  ProgramSequenceBindingMeme sequenceBindingMeme1839;
  ProgramSequenceBindingMeme sequenceBindingMeme1840;
  ProgramSequenceBindingMeme sequenceBindingMeme1841;
  ProgramSequenceBindingMeme sequenceBindingMeme1842;
  ProgramSequenceBindingMeme sequenceBindingMeme1843;
  ProgramSequenceBindingMeme sequenceBindingMeme1844;
  ProgramSequenceBindingMeme sequenceBindingMeme1845;
  ProgramSequenceBindingMeme sequenceBindingMeme1846;
  ProgramSequenceBindingMeme sequenceBindingMeme1847;
  ProgramSequenceBindingMeme sequenceBindingMeme1848;
  ProgramSequenceBindingMeme sequenceBindingMeme1849;
  ProgramSequenceBindingMeme sequenceBindingMeme1850;
  ProgramSequenceBindingMeme sequenceBindingMeme1851;
  ProgramSequenceBindingMeme sequenceBindingMeme1852;
  ProgramSequenceBindingMeme sequenceBindingMeme1853;
  Program program59;
  ProgramSequence sequence1854;
  ProgramSequence sequence1855;
  ProgramSequence sequence1856;
  ProgramSequenceChord sequenceChord1857;
  ProgramSequenceChord sequenceChord1858;
  ProgramSequenceChord sequenceChord1859;
  ProgramSequenceChord sequenceChord1860;
  ProgramSequenceChord sequenceChord1861;
  ProgramSequenceChord sequenceChord1862;
  ProgramSequenceChord sequenceChord1863;
  ProgramSequenceChord sequenceChord1864;
  ProgramSequenceChord sequenceChord1865;
  ProgramSequenceChord sequenceChord1866;
  ProgramSequenceChord sequenceChord1867;
  ProgramSequenceChord sequenceChord1868;
  ProgramSequenceChord sequenceChord1869;
  ProgramSequenceChord sequenceChord1870;
  ProgramSequenceChord sequenceChord1871;
  ProgramSequenceBinding sequenceBinding1872;
  ProgramSequenceBinding sequenceBinding1873;
  ProgramSequenceBinding sequenceBinding1874;
  ProgramSequenceBinding sequenceBinding1875;
  ProgramSequenceBinding sequenceBinding1876;
  ProgramSequenceBinding sequenceBinding1877;
  ProgramSequenceBinding sequenceBinding1878;
  ProgramSequenceBinding sequenceBinding1879;
  ProgramSequenceBinding sequenceBinding1880;
  ProgramSequenceBinding sequenceBinding1881;
  ProgramSequenceBinding sequenceBinding1882;
  ProgramSequenceBinding sequenceBinding1883;
  ProgramSequenceBinding sequenceBinding1884;
  ProgramSequenceBinding sequenceBinding1885;
  ProgramSequenceBinding sequenceBinding1886;
  ProgramSequenceBinding sequenceBinding1887;
  ProgramSequenceBinding sequenceBinding1888;
  ProgramSequenceBinding sequenceBinding1889;
  ProgramSequenceBinding sequenceBinding1890;
  ProgramSequenceBinding sequenceBinding1891;
  ProgramSequenceBinding sequenceBinding1892;
  ProgramSequenceBinding sequenceBinding1893;
  ProgramSequenceBindingMeme sequenceBindingMeme1894;
  ProgramSequenceBindingMeme sequenceBindingMeme1895;
  ProgramSequenceBindingMeme sequenceBindingMeme1896;
  ProgramSequenceBindingMeme sequenceBindingMeme1897;
  ProgramSequenceBindingMeme sequenceBindingMeme1898;
  ProgramSequenceBindingMeme sequenceBindingMeme1899;
  ProgramSequenceBindingMeme sequenceBindingMeme1900;
  ProgramSequenceBindingMeme sequenceBindingMeme1901;
  ProgramSequenceBindingMeme sequenceBindingMeme1902;
  ProgramSequenceBindingMeme sequenceBindingMeme1903;
  ProgramSequenceBindingMeme sequenceBindingMeme1904;
  ProgramSequenceBindingMeme sequenceBindingMeme1905;
  ProgramSequenceBindingMeme sequenceBindingMeme1906;
  ProgramSequenceBindingMeme sequenceBindingMeme1907;
  ProgramSequenceBindingMeme sequenceBindingMeme1908;
  ProgramSequenceBindingMeme sequenceBindingMeme1909;
  ProgramSequenceBindingMeme sequenceBindingMeme1910;
  ProgramSequenceBindingMeme sequenceBindingMeme1911;
  ProgramSequenceBindingMeme sequenceBindingMeme1912;
  ProgramSequenceBindingMeme sequenceBindingMeme1913;
  ProgramSequenceBindingMeme sequenceBindingMeme1914;
  ProgramSequenceBindingMeme sequenceBindingMeme1915;
  Program program48;
  ProgramSequence sequence1916;
  ProgramSequence sequence1917;
  ProgramSequence sequence1918;
  ProgramSequence sequence1919;
  ProgramSequence sequence1920;
  ProgramSequence sequence1921;
  ProgramSequenceChord sequenceChord1922;
  ProgramSequenceChord sequenceChord1923;
  ProgramSequenceChord sequenceChord1924;
  ProgramSequenceChord sequenceChord1925;
  ProgramSequenceChord sequenceChord1926;
  ProgramSequenceChord sequenceChord1927;
  ProgramSequenceChord sequenceChord1928;
  ProgramSequenceChord sequenceChord1929;
  ProgramSequenceChord sequenceChord1930;
  ProgramSequenceChord sequenceChord1931;
  ProgramSequenceChord sequenceChord1932;
  ProgramSequenceChord sequenceChord1933;
  ProgramSequenceChord sequenceChord1934;
  ProgramSequenceChord sequenceChord1935;
  ProgramSequenceChord sequenceChord1936;
  ProgramSequenceChord sequenceChord1937;
  ProgramSequenceChord sequenceChord1938;
  ProgramSequenceChord sequenceChord1939;
  ProgramSequenceChord sequenceChord1940;
  ProgramSequenceChord sequenceChord1941;
  ProgramSequenceBinding sequenceBinding1942;
  ProgramSequenceBinding sequenceBinding1943;
  ProgramSequenceBinding sequenceBinding1944;
  ProgramSequenceBinding sequenceBinding1945;
  ProgramSequenceBinding sequenceBinding1946;
  ProgramSequenceBinding sequenceBinding1947;
  ProgramSequenceBinding sequenceBinding1948;
  ProgramSequenceBinding sequenceBinding1949;
  ProgramSequenceBinding sequenceBinding1950;
  ProgramSequenceBinding sequenceBinding1951;
  ProgramSequenceBinding sequenceBinding1952;
  ProgramSequenceBinding sequenceBinding1953;
  ProgramSequenceBinding sequenceBinding1954;
  ProgramSequenceBinding sequenceBinding1955;
  ProgramSequenceBinding sequenceBinding1956;
  ProgramSequenceBinding sequenceBinding1957;
  ProgramSequenceBinding sequenceBinding1958;
  ProgramSequenceBinding sequenceBinding1959;
  ProgramSequenceBinding sequenceBinding1960;
  ProgramSequenceBinding sequenceBinding1961;
  ProgramSequenceBinding sequenceBinding1962;
  ProgramSequenceBinding sequenceBinding1963;
  ProgramSequenceBinding sequenceBinding1964;
  ProgramSequenceBinding sequenceBinding1965;
  ProgramSequenceBinding sequenceBinding1966;
  ProgramSequenceBinding sequenceBinding1967;
  ProgramSequenceBinding sequenceBinding1968;
  ProgramSequenceBinding sequenceBinding1969;
  ProgramSequenceBinding sequenceBinding1970;
  ProgramSequenceBindingMeme sequenceBindingMeme1971;
  ProgramSequenceBindingMeme sequenceBindingMeme1972;
  ProgramSequenceBindingMeme sequenceBindingMeme1973;
  ProgramSequenceBindingMeme sequenceBindingMeme1974;
  ProgramSequenceBindingMeme sequenceBindingMeme1975;
  ProgramSequenceBindingMeme sequenceBindingMeme1976;
  ProgramSequenceBindingMeme sequenceBindingMeme1977;
  ProgramSequenceBindingMeme sequenceBindingMeme1978;
  ProgramSequenceBindingMeme sequenceBindingMeme1979;
  ProgramSequenceBindingMeme sequenceBindingMeme1980;
  ProgramSequenceBindingMeme sequenceBindingMeme1981;
  ProgramSequenceBindingMeme sequenceBindingMeme1982;
  ProgramSequenceBindingMeme sequenceBindingMeme1983;
  ProgramSequenceBindingMeme sequenceBindingMeme1984;
  ProgramSequenceBindingMeme sequenceBindingMeme1985;
  ProgramSequenceBindingMeme sequenceBindingMeme1986;
  ProgramSequenceBindingMeme sequenceBindingMeme1987;
  ProgramSequenceBindingMeme sequenceBindingMeme1988;
  ProgramSequenceBindingMeme sequenceBindingMeme1989;
  ProgramSequenceBindingMeme sequenceBindingMeme1990;
  ProgramSequenceBindingMeme sequenceBindingMeme1991;
  ProgramSequenceBindingMeme sequenceBindingMeme1992;
  ProgramSequenceBindingMeme sequenceBindingMeme1993;
  ProgramSequenceBindingMeme sequenceBindingMeme1994;
  ProgramSequenceBindingMeme sequenceBindingMeme1995;
  ProgramSequenceBindingMeme sequenceBindingMeme1996;
  ProgramSequenceBindingMeme sequenceBindingMeme1997;
  ProgramSequenceBindingMeme sequenceBindingMeme1998;
  ProgramSequenceBindingMeme sequenceBindingMeme1999;
  Program program35;
  ProgramSequence sequence2000;
  ProgramSequence sequence2001;
  ProgramSequenceChord sequenceChord2002;
  ProgramSequenceChord sequenceChord2003;
  ProgramSequenceChord sequenceChord2004;
  ProgramSequenceChord sequenceChord2005;
  ProgramSequenceChord sequenceChord2006;
  ProgramSequenceChord sequenceChord2007;
  ProgramSequenceChord sequenceChord2008;
  ProgramSequenceChord sequenceChord2009;
  ProgramSequenceChord sequenceChord2010;
  ProgramSequenceChord sequenceChord2011;
  ProgramSequenceBinding sequenceBinding2012;
  ProgramSequenceBinding sequenceBinding2013;
  ProgramSequenceBinding sequenceBinding2014;
  ProgramSequenceBinding sequenceBinding2015;
  ProgramSequenceBinding sequenceBinding2016;
  ProgramSequenceBinding sequenceBinding2017;
  ProgramSequenceBinding sequenceBinding2018;
  ProgramSequenceBinding sequenceBinding2019;
  ProgramSequenceBinding sequenceBinding2020;
  ProgramSequenceBinding sequenceBinding2021;
  ProgramSequenceBinding sequenceBinding2022;
  ProgramSequenceBinding sequenceBinding2023;
  ProgramSequenceBinding sequenceBinding2024;
  ProgramSequenceBinding sequenceBinding2025;
  ProgramSequenceBinding sequenceBinding2026;
  ProgramSequenceBinding sequenceBinding2027;
  ProgramSequenceBinding sequenceBinding2028;
  ProgramSequenceBinding sequenceBinding2029;
  ProgramSequenceBinding sequenceBinding2030;
  ProgramSequenceBinding sequenceBinding2031;
  ProgramSequenceBinding sequenceBinding2032;
  ProgramSequenceBinding sequenceBinding2033;
  ProgramSequenceBindingMeme sequenceBindingMeme2034;
  ProgramSequenceBindingMeme sequenceBindingMeme2035;
  ProgramSequenceBindingMeme sequenceBindingMeme2036;
  ProgramSequenceBindingMeme sequenceBindingMeme2037;
  ProgramSequenceBindingMeme sequenceBindingMeme2038;
  ProgramSequenceBindingMeme sequenceBindingMeme2039;
  ProgramSequenceBindingMeme sequenceBindingMeme2040;
  ProgramSequenceBindingMeme sequenceBindingMeme2041;
  ProgramSequenceBindingMeme sequenceBindingMeme2042;
  ProgramSequenceBindingMeme sequenceBindingMeme2043;
  ProgramSequenceBindingMeme sequenceBindingMeme2044;
  ProgramSequenceBindingMeme sequenceBindingMeme2045;
  ProgramSequenceBindingMeme sequenceBindingMeme2046;
  ProgramSequenceBindingMeme sequenceBindingMeme2047;
  ProgramSequenceBindingMeme sequenceBindingMeme2048;
  ProgramSequenceBindingMeme sequenceBindingMeme2049;
  ProgramSequenceBindingMeme sequenceBindingMeme2050;
  ProgramSequenceBindingMeme sequenceBindingMeme2051;
  ProgramSequenceBindingMeme sequenceBindingMeme2052;
  ProgramSequenceBindingMeme sequenceBindingMeme2053;
  ProgramSequenceBindingMeme sequenceBindingMeme2054;
  ProgramSequenceBindingMeme sequenceBindingMeme2055;
  Program program64;
  ProgramSequence sequence2056;
  ProgramSequenceChord sequenceChord2057;
  ProgramSequenceChord sequenceChord2058;
  ProgramSequenceChord sequenceChord2059;
  ProgramSequenceChord sequenceChord2060;
  ProgramSequenceChord sequenceChord2061;
  ProgramSequenceChord sequenceChord2062;
  ProgramSequenceChord sequenceChord2063;
  ProgramSequenceChord sequenceChord2064;
  ProgramSequenceChord sequenceChord2065;
  ProgramSequenceChord sequenceChord2066;
  ProgramSequenceBinding sequenceBinding2067;
  ProgramSequenceBinding sequenceBinding2068;
  ProgramSequenceBinding sequenceBinding2069;
  ProgramSequenceBinding sequenceBinding2070;
  ProgramSequenceBinding sequenceBinding2071;
  ProgramSequenceBinding sequenceBinding2072;
  ProgramSequenceBinding sequenceBinding2073;
  ProgramSequenceBinding sequenceBinding2074;
  ProgramSequenceBinding sequenceBinding2075;
  ProgramSequenceBinding sequenceBinding2076;
  ProgramSequenceBindingMeme sequenceBindingMeme2077;
  ProgramSequenceBindingMeme sequenceBindingMeme2078;
  ProgramSequenceBindingMeme sequenceBindingMeme2079;
  ProgramSequenceBindingMeme sequenceBindingMeme2080;
  ProgramSequenceBindingMeme sequenceBindingMeme2081;
  ProgramSequenceBindingMeme sequenceBindingMeme2082;
  ProgramSequenceBindingMeme sequenceBindingMeme2083;
  ProgramSequenceBindingMeme sequenceBindingMeme2084;
  ProgramSequenceBindingMeme sequenceBindingMeme2085;
  ProgramSequenceBindingMeme sequenceBindingMeme2086;
  Program program80;
  ProgramSequence sequence2087;
  ProgramSequence sequence2088;
  ProgramSequence sequence2089;
  ProgramSequence sequence2090;
  ProgramSequenceChord sequenceChord2091;
  ProgramSequenceChord sequenceChord2092;
  ProgramSequenceChord sequenceChord2093;
  ProgramSequenceChord sequenceChord2094;
  ProgramSequenceChord sequenceChord2095;
  ProgramSequenceChord sequenceChord2096;
  ProgramSequenceChord sequenceChord2097;
  ProgramSequenceBinding sequenceBinding2098;
  ProgramSequenceBinding sequenceBinding2099;
  ProgramSequenceBinding sequenceBinding2100;
  ProgramSequenceBinding sequenceBinding2101;
  ProgramSequenceBinding sequenceBinding2102;
  ProgramSequenceBinding sequenceBinding2103;
  ProgramSequenceBinding sequenceBinding2104;
  ProgramSequenceBinding sequenceBinding2105;
  ProgramSequenceBinding sequenceBinding2106;
  ProgramSequenceBinding sequenceBinding2107;
  ProgramSequenceBinding sequenceBinding2108;
  ProgramSequenceBinding sequenceBinding2109;
  ProgramSequenceBinding sequenceBinding2110;
  ProgramSequenceBinding sequenceBinding2111;
  ProgramSequenceBinding sequenceBinding2112;
  ProgramSequenceBinding sequenceBinding2113;
  ProgramSequenceBinding sequenceBinding2114;
  ProgramSequenceBinding sequenceBinding2115;
  ProgramSequenceBinding sequenceBinding2116;
  ProgramSequenceBinding sequenceBinding2117;
  ProgramSequenceBindingMeme sequenceBindingMeme2118;
  ProgramSequenceBindingMeme sequenceBindingMeme2119;
  ProgramSequenceBindingMeme sequenceBindingMeme2120;
  ProgramSequenceBindingMeme sequenceBindingMeme2121;
  ProgramSequenceBindingMeme sequenceBindingMeme2122;
  ProgramSequenceBindingMeme sequenceBindingMeme2123;
  ProgramSequenceBindingMeme sequenceBindingMeme2124;
  ProgramSequenceBindingMeme sequenceBindingMeme2125;
  ProgramSequenceBindingMeme sequenceBindingMeme2126;
  ProgramSequenceBindingMeme sequenceBindingMeme2127;
  ProgramSequenceBindingMeme sequenceBindingMeme2128;
  ProgramSequenceBindingMeme sequenceBindingMeme2129;
  ProgramSequenceBindingMeme sequenceBindingMeme2130;
  ProgramSequenceBindingMeme sequenceBindingMeme2131;
  ProgramSequenceBindingMeme sequenceBindingMeme2132;
  ProgramSequenceBindingMeme sequenceBindingMeme2133;
  ProgramSequenceBindingMeme sequenceBindingMeme2134;
  ProgramSequenceBindingMeme sequenceBindingMeme2135;
  ProgramSequenceBindingMeme sequenceBindingMeme2136;
  ProgramSequenceBindingMeme sequenceBindingMeme2137;
  Program program86;
  ProgramSequence sequence2138;
  ProgramSequence sequence2139;
  ProgramSequence sequence2140;
  ProgramSequenceChord sequenceChord2141;
  ProgramSequenceChord sequenceChord2142;
  ProgramSequenceChord sequenceChord2143;
  ProgramSequenceChord sequenceChord2144;
  ProgramSequenceChord sequenceChord2145;
  ProgramSequenceChord sequenceChord2146;
  ProgramSequenceChord sequenceChord2147;
  ProgramSequenceChord sequenceChord2148;
  ProgramSequenceChord sequenceChord2149;
  ProgramSequenceChord sequenceChord2150;
  ProgramSequenceChord sequenceChord2151;
  ProgramSequenceChord sequenceChord2152;
  ProgramSequenceBinding sequenceBinding2153;
  ProgramSequenceBinding sequenceBinding2154;
  ProgramSequenceBinding sequenceBinding2155;
  ProgramSequenceBinding sequenceBinding2156;
  ProgramSequenceBinding sequenceBinding2157;
  ProgramSequenceBinding sequenceBinding2158;
  ProgramSequenceBinding sequenceBinding2159;
  ProgramSequenceBinding sequenceBinding2160;
  ProgramSequenceBinding sequenceBinding2161;
  ProgramSequenceBinding sequenceBinding2162;
  ProgramSequenceBinding sequenceBinding2163;
  ProgramSequenceBinding sequenceBinding2164;
  ProgramSequenceBinding sequenceBinding2165;
  ProgramSequenceBinding sequenceBinding2166;
  ProgramSequenceBinding sequenceBinding2167;
  ProgramSequenceBinding sequenceBinding2168;
  ProgramSequenceBinding sequenceBinding2169;
  ProgramSequenceBinding sequenceBinding2170;
  ProgramSequenceBinding sequenceBinding2171;
  ProgramSequenceBinding sequenceBinding2172;
  ProgramSequenceBinding sequenceBinding2173;
  ProgramSequenceBinding sequenceBinding2174;
  ProgramSequenceBinding sequenceBinding2175;
  ProgramSequenceBinding sequenceBinding2176;
  ProgramSequenceBinding sequenceBinding2177;
  ProgramSequenceBindingMeme sequenceBindingMeme2178;
  ProgramSequenceBindingMeme sequenceBindingMeme2179;
  ProgramSequenceBindingMeme sequenceBindingMeme2180;
  ProgramSequenceBindingMeme sequenceBindingMeme2181;
  ProgramSequenceBindingMeme sequenceBindingMeme2182;
  ProgramSequenceBindingMeme sequenceBindingMeme2183;
  ProgramSequenceBindingMeme sequenceBindingMeme2184;
  ProgramSequenceBindingMeme sequenceBindingMeme2185;
  ProgramSequenceBindingMeme sequenceBindingMeme2186;
  ProgramSequenceBindingMeme sequenceBindingMeme2187;
  ProgramSequenceBindingMeme sequenceBindingMeme2188;
  ProgramSequenceBindingMeme sequenceBindingMeme2189;
  ProgramSequenceBindingMeme sequenceBindingMeme2190;
  ProgramSequenceBindingMeme sequenceBindingMeme2191;
  ProgramSequenceBindingMeme sequenceBindingMeme2192;
  ProgramSequenceBindingMeme sequenceBindingMeme2193;
  ProgramSequenceBindingMeme sequenceBindingMeme2194;
  ProgramSequenceBindingMeme sequenceBindingMeme2195;
  ProgramSequenceBindingMeme sequenceBindingMeme2196;
  ProgramSequenceBindingMeme sequenceBindingMeme2197;
  ProgramSequenceBindingMeme sequenceBindingMeme2198;
  ProgramSequenceBindingMeme sequenceBindingMeme2199;
  ProgramSequenceBindingMeme sequenceBindingMeme2200;
  ProgramSequenceBindingMeme sequenceBindingMeme2201;
  ProgramSequenceBindingMeme sequenceBindingMeme2202;
  Program program60;
  ProgramSequence sequence2203;
  ProgramSequence sequence2204;
  ProgramSequenceChord sequenceChord2205;
  ProgramSequenceChord sequenceChord2206;
  ProgramSequenceChord sequenceChord2207;
  ProgramSequenceChord sequenceChord2208;
  ProgramSequenceChord sequenceChord2209;
  ProgramSequenceChord sequenceChord2210;
  ProgramSequenceChord sequenceChord2211;
  ProgramSequenceChord sequenceChord2212;
  ProgramSequenceChord sequenceChord2213;
  ProgramSequenceBinding sequenceBinding2214;
  ProgramSequenceBinding sequenceBinding2215;
  ProgramSequenceBinding sequenceBinding2216;
  ProgramSequenceBinding sequenceBinding2217;
  ProgramSequenceBinding sequenceBinding2218;
  ProgramSequenceBinding sequenceBinding2219;
  ProgramSequenceBinding sequenceBinding2220;
  ProgramSequenceBinding sequenceBinding2221;
  ProgramSequenceBinding sequenceBinding2222;
  ProgramSequenceBinding sequenceBinding2223;
  ProgramSequenceBinding sequenceBinding2224;
  ProgramSequenceBinding sequenceBinding2225;
  ProgramSequenceBinding sequenceBinding2226;
  ProgramSequenceBinding sequenceBinding2227;
  ProgramSequenceBinding sequenceBinding2228;
  ProgramSequenceBinding sequenceBinding2229;
  ProgramSequenceBinding sequenceBinding2230;
  ProgramSequenceBinding sequenceBinding2231;
  ProgramSequenceBindingMeme sequenceBindingMeme2232;
  ProgramSequenceBindingMeme sequenceBindingMeme2233;
  ProgramSequenceBindingMeme sequenceBindingMeme2234;
  ProgramSequenceBindingMeme sequenceBindingMeme2235;
  ProgramSequenceBindingMeme sequenceBindingMeme2236;
  ProgramSequenceBindingMeme sequenceBindingMeme2237;
  ProgramSequenceBindingMeme sequenceBindingMeme2238;
  ProgramSequenceBindingMeme sequenceBindingMeme2239;
  ProgramSequenceBindingMeme sequenceBindingMeme2240;
  ProgramSequenceBindingMeme sequenceBindingMeme2241;
  ProgramSequenceBindingMeme sequenceBindingMeme2242;
  ProgramSequenceBindingMeme sequenceBindingMeme2243;
  ProgramSequenceBindingMeme sequenceBindingMeme2244;
  ProgramSequenceBindingMeme sequenceBindingMeme2245;
  ProgramSequenceBindingMeme sequenceBindingMeme2246;
  ProgramSequenceBindingMeme sequenceBindingMeme2247;
  ProgramSequenceBindingMeme sequenceBindingMeme2248;
  ProgramSequenceBindingMeme sequenceBindingMeme2249;
  Program program89;
  ProgramSequence sequence2250;
  ProgramSequence sequence2251;
  ProgramSequence sequence2252;
  ProgramSequenceChord sequenceChord2253;
  ProgramSequenceChord sequenceChord2254;
  ProgramSequenceChord sequenceChord2255;
  ProgramSequenceChord sequenceChord2256;
  ProgramSequenceChord sequenceChord2257;
  ProgramSequenceChord sequenceChord2258;
  ProgramSequenceChord sequenceChord2259;
  ProgramSequenceChord sequenceChord2260;
  ProgramSequenceChord sequenceChord2261;
  ProgramSequenceChord sequenceChord2262;
  ProgramSequenceChord sequenceChord2263;
  ProgramSequenceBinding sequenceBinding2264;
  ProgramSequenceBinding sequenceBinding2265;
  ProgramSequenceBinding sequenceBinding2266;
  ProgramSequenceBinding sequenceBinding2267;
  ProgramSequenceBinding sequenceBinding2268;
  ProgramSequenceBinding sequenceBinding2269;
  ProgramSequenceBinding sequenceBinding2270;
  ProgramSequenceBinding sequenceBinding2271;
  ProgramSequenceBinding sequenceBinding2272;
  ProgramSequenceBinding sequenceBinding2273;
  ProgramSequenceBinding sequenceBinding2274;
  ProgramSequenceBinding sequenceBinding2275;
  ProgramSequenceBinding sequenceBinding2276;
  ProgramSequenceBinding sequenceBinding2277;
  ProgramSequenceBinding sequenceBinding2278;
  ProgramSequenceBinding sequenceBinding2279;
  ProgramSequenceBinding sequenceBinding2280;
  ProgramSequenceBindingMeme sequenceBindingMeme2281;
  ProgramSequenceBindingMeme sequenceBindingMeme2282;
  ProgramSequenceBindingMeme sequenceBindingMeme2283;
  ProgramSequenceBindingMeme sequenceBindingMeme2284;
  ProgramSequenceBindingMeme sequenceBindingMeme2285;
  ProgramSequenceBindingMeme sequenceBindingMeme2286;
  ProgramSequenceBindingMeme sequenceBindingMeme2287;
  ProgramSequenceBindingMeme sequenceBindingMeme2288;
  ProgramSequenceBindingMeme sequenceBindingMeme2289;
  ProgramSequenceBindingMeme sequenceBindingMeme2290;
  ProgramSequenceBindingMeme sequenceBindingMeme2291;
  ProgramSequenceBindingMeme sequenceBindingMeme2292;
  ProgramSequenceBindingMeme sequenceBindingMeme2293;
  ProgramSequenceBindingMeme sequenceBindingMeme2294;
  ProgramSequenceBindingMeme sequenceBindingMeme2295;
  ProgramSequenceBindingMeme sequenceBindingMeme2296;
  ProgramSequenceBindingMeme sequenceBindingMeme2297;
  Program program76;
  ProgramSequence sequence2298;
  ProgramSequence sequence2299;
  ProgramSequence sequence2300;
  ProgramSequence sequence2301;
  ProgramSequenceChord sequenceChord2302;
  ProgramSequenceChord sequenceChord2303;
  ProgramSequenceChord sequenceChord2304;
  ProgramSequenceChord sequenceChord2305;
  ProgramSequenceChord sequenceChord2306;
  ProgramSequenceChord sequenceChord2307;
  ProgramSequenceChord sequenceChord2308;
  ProgramSequenceChord sequenceChord2309;
  ProgramSequenceChord sequenceChord2310;
  ProgramSequenceChord sequenceChord2311;
  ProgramSequenceChord sequenceChord2312;
  ProgramSequenceBinding sequenceBinding2313;
  ProgramSequenceBinding sequenceBinding2314;
  ProgramSequenceBinding sequenceBinding2315;
  ProgramSequenceBinding sequenceBinding2316;
  ProgramSequenceBinding sequenceBinding2317;
  ProgramSequenceBinding sequenceBinding2318;
  ProgramSequenceBinding sequenceBinding2319;
  ProgramSequenceBinding sequenceBinding2320;
  ProgramSequenceBinding sequenceBinding2321;
  ProgramSequenceBinding sequenceBinding2322;
  ProgramSequenceBinding sequenceBinding2323;
  ProgramSequenceBinding sequenceBinding2324;
  ProgramSequenceBinding sequenceBinding2325;
  ProgramSequenceBinding sequenceBinding2326;
  ProgramSequenceBinding sequenceBinding2327;
  ProgramSequenceBinding sequenceBinding2328;
  ProgramSequenceBinding sequenceBinding2329;
  ProgramSequenceBinding sequenceBinding2330;
  ProgramSequenceBinding sequenceBinding2331;
  ProgramSequenceBinding sequenceBinding2332;
  ProgramSequenceBinding sequenceBinding2333;
  ProgramSequenceBinding sequenceBinding2334;
  ProgramSequenceBinding sequenceBinding2335;
  ProgramSequenceBinding sequenceBinding2336;
  ProgramSequenceBinding sequenceBinding2337;
  ProgramSequenceBinding sequenceBinding2338;
  ProgramSequenceBindingMeme sequenceBindingMeme2339;
  ProgramSequenceBindingMeme sequenceBindingMeme2340;
  ProgramSequenceBindingMeme sequenceBindingMeme2341;
  ProgramSequenceBindingMeme sequenceBindingMeme2342;
  ProgramSequenceBindingMeme sequenceBindingMeme2343;
  ProgramSequenceBindingMeme sequenceBindingMeme2344;
  ProgramSequenceBindingMeme sequenceBindingMeme2345;
  ProgramSequenceBindingMeme sequenceBindingMeme2346;
  ProgramSequenceBindingMeme sequenceBindingMeme2347;
  ProgramSequenceBindingMeme sequenceBindingMeme2348;
  ProgramSequenceBindingMeme sequenceBindingMeme2349;
  ProgramSequenceBindingMeme sequenceBindingMeme2350;
  ProgramSequenceBindingMeme sequenceBindingMeme2351;
  ProgramSequenceBindingMeme sequenceBindingMeme2352;
  ProgramSequenceBindingMeme sequenceBindingMeme2353;
  ProgramSequenceBindingMeme sequenceBindingMeme2354;
  ProgramSequenceBindingMeme sequenceBindingMeme2355;
  ProgramSequenceBindingMeme sequenceBindingMeme2356;
  ProgramSequenceBindingMeme sequenceBindingMeme2357;
  ProgramSequenceBindingMeme sequenceBindingMeme2358;
  ProgramSequenceBindingMeme sequenceBindingMeme2359;
  ProgramSequenceBindingMeme sequenceBindingMeme2360;
  ProgramSequenceBindingMeme sequenceBindingMeme2361;
  ProgramSequenceBindingMeme sequenceBindingMeme2362;
  ProgramSequenceBindingMeme sequenceBindingMeme2363;
  Program program9;
  ProgramSequence sequence2364;
  ProgramSequence sequence2365;
  ProgramSequence sequence2366;
  ProgramSequenceChord sequenceChord2367;
  ProgramSequenceChord sequenceChord2368;
  ProgramSequenceChord sequenceChord2369;
  ProgramSequenceChord sequenceChord2370;
  ProgramSequenceChord sequenceChord2371;
  ProgramSequenceChord sequenceChord2372;
  ProgramSequenceChord sequenceChord2373;
  ProgramSequenceChord sequenceChord2374;
  ProgramSequenceChord sequenceChord2375;
  ProgramSequenceChord sequenceChord2376;
  ProgramSequenceChord sequenceChord2377;
  ProgramSequenceChord sequenceChord2378;
  ProgramSequenceChord sequenceChord2379;
  ProgramSequenceChord sequenceChord2380;
  ProgramSequenceChord sequenceChord2381;
  ProgramSequenceBinding sequenceBinding2382;
  ProgramSequenceBinding sequenceBinding2383;
  ProgramSequenceBinding sequenceBinding2384;
  ProgramSequenceBindingMeme sequenceBindingMeme2385;
  ProgramSequenceBindingMeme sequenceBindingMeme2386;
  ProgramSequenceBindingMeme sequenceBindingMeme2387;
  ProgramSequenceBindingMeme sequenceBindingMeme2388;
  Program program84;
  ProgramSequence sequence2389;
  ProgramSequence sequence2390;
  ProgramSequenceChord sequenceChord2391;
  ProgramSequenceChord sequenceChord2392;
  ProgramSequenceChord sequenceChord2393;
  ProgramSequenceChord sequenceChord2394;
  ProgramSequenceChord sequenceChord2395;
  ProgramSequenceBinding sequenceBinding2396;
  ProgramSequenceBinding sequenceBinding2397;
  ProgramSequenceBinding sequenceBinding2398;
  ProgramSequenceBinding sequenceBinding2399;
  ProgramSequenceBinding sequenceBinding2400;
  ProgramSequenceBinding sequenceBinding2401;
  ProgramSequenceBinding sequenceBinding2402;
  ProgramSequenceBinding sequenceBinding2403;
  ProgramSequenceBinding sequenceBinding2404;
  ProgramSequenceBinding sequenceBinding2405;
  ProgramSequenceBinding sequenceBinding2406;
  ProgramSequenceBinding sequenceBinding2407;
  ProgramSequenceBinding sequenceBinding2408;
  ProgramSequenceBinding sequenceBinding2409;
  ProgramSequenceBinding sequenceBinding2410;
  ProgramSequenceBinding sequenceBinding2411;
  ProgramSequenceBinding sequenceBinding2412;
  ProgramSequenceBinding sequenceBinding2413;
  ProgramSequenceBindingMeme sequenceBindingMeme2414;
  ProgramSequenceBindingMeme sequenceBindingMeme2415;
  ProgramSequenceBindingMeme sequenceBindingMeme2416;
  ProgramSequenceBindingMeme sequenceBindingMeme2417;
  ProgramSequenceBindingMeme sequenceBindingMeme2418;
  ProgramSequenceBindingMeme sequenceBindingMeme2419;
  ProgramSequenceBindingMeme sequenceBindingMeme2420;
  ProgramSequenceBindingMeme sequenceBindingMeme2421;
  ProgramSequenceBindingMeme sequenceBindingMeme2422;
  ProgramSequenceBindingMeme sequenceBindingMeme2423;
  ProgramSequenceBindingMeme sequenceBindingMeme2424;
  ProgramSequenceBindingMeme sequenceBindingMeme2425;
  ProgramSequenceBindingMeme sequenceBindingMeme2426;
  ProgramSequenceBindingMeme sequenceBindingMeme2427;
  ProgramSequenceBindingMeme sequenceBindingMeme2428;
  ProgramSequenceBindingMeme sequenceBindingMeme2429;
  ProgramSequenceBindingMeme sequenceBindingMeme2430;
  ProgramSequenceBindingMeme sequenceBindingMeme2431;
  Program program11;
  ProgramSequence sequence2432;
  ProgramSequence sequence2433;
  ProgramSequence sequence2434;
  ProgramSequenceChord sequenceChord2435;
  ProgramSequenceChord sequenceChord2436;
  ProgramSequenceChord sequenceChord2437;
  ProgramSequenceChord sequenceChord2438;
  ProgramSequenceChord sequenceChord2439;
  ProgramSequenceChord sequenceChord2440;
  ProgramSequenceChord sequenceChord2441;
  ProgramSequenceChord sequenceChord2442;
  ProgramSequenceBinding sequenceBinding2443;
  ProgramSequenceBinding sequenceBinding2444;
  ProgramSequenceBinding sequenceBinding2445;
  ProgramSequenceBinding sequenceBinding2446;
  ProgramSequenceBinding sequenceBinding2447;
  ProgramSequenceBinding sequenceBinding2448;
  ProgramSequenceBinding sequenceBinding2449;
  ProgramSequenceBinding sequenceBinding2450;
  ProgramSequenceBinding sequenceBinding2451;
  ProgramSequenceBinding sequenceBinding2452;
  ProgramSequenceBinding sequenceBinding2453;
  ProgramSequenceBinding sequenceBinding2454;
  ProgramSequenceBinding sequenceBinding2455;
  ProgramSequenceBinding sequenceBinding2456;
  ProgramSequenceBinding sequenceBinding2457;
  ProgramSequenceBinding sequenceBinding2458;
  ProgramSequenceBinding sequenceBinding2459;
  ProgramSequenceBinding sequenceBinding2460;
  ProgramSequenceBinding sequenceBinding2461;
  ProgramSequenceBinding sequenceBinding2462;
  ProgramSequenceBinding sequenceBinding2463;
  ProgramSequenceBinding sequenceBinding2464;
  ProgramSequenceBinding sequenceBinding2465;
  ProgramSequenceBinding sequenceBinding2466;
  ProgramSequenceBinding sequenceBinding2467;
  ProgramSequenceBinding sequenceBinding2468;
  ProgramSequenceBinding sequenceBinding2469;
  ProgramSequenceBindingMeme sequenceBindingMeme2470;
  ProgramSequenceBindingMeme sequenceBindingMeme2471;
  ProgramSequenceBindingMeme sequenceBindingMeme2472;
  ProgramSequenceBindingMeme sequenceBindingMeme2473;
  ProgramSequenceBindingMeme sequenceBindingMeme2474;
  ProgramSequenceBindingMeme sequenceBindingMeme2475;
  ProgramSequenceBindingMeme sequenceBindingMeme2476;
  ProgramSequenceBindingMeme sequenceBindingMeme2477;
  ProgramSequenceBindingMeme sequenceBindingMeme2478;
  ProgramSequenceBindingMeme sequenceBindingMeme2479;
  ProgramSequenceBindingMeme sequenceBindingMeme2480;
  ProgramSequenceBindingMeme sequenceBindingMeme2481;
  ProgramSequenceBindingMeme sequenceBindingMeme2482;
  ProgramSequenceBindingMeme sequenceBindingMeme2483;
  ProgramSequenceBindingMeme sequenceBindingMeme2484;
  ProgramSequenceBindingMeme sequenceBindingMeme2485;
  ProgramSequenceBindingMeme sequenceBindingMeme2486;
  ProgramSequenceBindingMeme sequenceBindingMeme2487;
  ProgramSequenceBindingMeme sequenceBindingMeme2488;
  ProgramSequenceBindingMeme sequenceBindingMeme2489;
  ProgramSequenceBindingMeme sequenceBindingMeme2490;
  ProgramSequenceBindingMeme sequenceBindingMeme2491;
  ProgramSequenceBindingMeme sequenceBindingMeme2492;
  ProgramSequenceBindingMeme sequenceBindingMeme2493;
  ProgramSequenceBindingMeme sequenceBindingMeme2494;
  ProgramSequenceBindingMeme sequenceBindingMeme2495;
  ProgramSequenceBindingMeme sequenceBindingMeme2496;
  Program program49;
  ProgramSequence sequence2497;
  ProgramSequence sequence2498;
  ProgramSequence sequence2499;
  ProgramSequenceChord sequenceChord2500;
  ProgramSequenceChord sequenceChord2501;
  ProgramSequenceChord sequenceChord2502;
  ProgramSequenceChord sequenceChord2503;
  ProgramSequenceChord sequenceChord2504;
  ProgramSequenceChord sequenceChord2505;
  ProgramSequenceChord sequenceChord2506;
  ProgramSequenceChord sequenceChord2507;
  ProgramSequenceChord sequenceChord2508;
  ProgramSequenceChord sequenceChord2509;
  ProgramSequenceChord sequenceChord2510;
  ProgramSequenceChord sequenceChord2511;
  ProgramSequenceBinding sequenceBinding2512;
  ProgramSequenceBinding sequenceBinding2513;
  ProgramSequenceBinding sequenceBinding2514;
  ProgramSequenceBinding sequenceBinding2515;
  ProgramSequenceBinding sequenceBinding2516;
  ProgramSequenceBinding sequenceBinding2517;
  ProgramSequenceBinding sequenceBinding2518;
  ProgramSequenceBinding sequenceBinding2519;
  ProgramSequenceBinding sequenceBinding2520;
  ProgramSequenceBinding sequenceBinding2521;
  ProgramSequenceBinding sequenceBinding2522;
  ProgramSequenceBinding sequenceBinding2523;
  ProgramSequenceBinding sequenceBinding2524;
  ProgramSequenceBinding sequenceBinding2525;
  ProgramSequenceBinding sequenceBinding2526;
  ProgramSequenceBinding sequenceBinding2527;
  ProgramSequenceBinding sequenceBinding2528;
  ProgramSequenceBinding sequenceBinding2529;
  ProgramSequenceBinding sequenceBinding2530;
  ProgramSequenceBinding sequenceBinding2531;
  ProgramSequenceBinding sequenceBinding2532;
  ProgramSequenceBinding sequenceBinding2533;
  ProgramSequenceBinding sequenceBinding2534;
  ProgramSequenceBinding sequenceBinding2535;
  ProgramSequenceBinding sequenceBinding2536;
  ProgramSequenceBinding sequenceBinding2537;
  ProgramSequenceBindingMeme sequenceBindingMeme2538;
  ProgramSequenceBindingMeme sequenceBindingMeme2539;
  ProgramSequenceBindingMeme sequenceBindingMeme2540;
  ProgramSequenceBindingMeme sequenceBindingMeme2541;
  ProgramSequenceBindingMeme sequenceBindingMeme2542;
  ProgramSequenceBindingMeme sequenceBindingMeme2543;
  ProgramSequenceBindingMeme sequenceBindingMeme2544;
  ProgramSequenceBindingMeme sequenceBindingMeme2545;
  ProgramSequenceBindingMeme sequenceBindingMeme2546;
  ProgramSequenceBindingMeme sequenceBindingMeme2547;
  ProgramSequenceBindingMeme sequenceBindingMeme2548;
  ProgramSequenceBindingMeme sequenceBindingMeme2549;
  ProgramSequenceBindingMeme sequenceBindingMeme2550;
  ProgramSequenceBindingMeme sequenceBindingMeme2551;
  ProgramSequenceBindingMeme sequenceBindingMeme2552;
  ProgramSequenceBindingMeme sequenceBindingMeme2553;
  ProgramSequenceBindingMeme sequenceBindingMeme2554;
  ProgramSequenceBindingMeme sequenceBindingMeme2555;
  ProgramSequenceBindingMeme sequenceBindingMeme2556;
  ProgramSequenceBindingMeme sequenceBindingMeme2557;
  ProgramSequenceBindingMeme sequenceBindingMeme2558;
  ProgramSequenceBindingMeme sequenceBindingMeme2559;
  ProgramSequenceBindingMeme sequenceBindingMeme2560;
  ProgramSequenceBindingMeme sequenceBindingMeme2561;
  ProgramSequenceBindingMeme sequenceBindingMeme2562;
  ProgramSequenceBindingMeme sequenceBindingMeme2563;
  Program program87;
  ProgramSequence sequence2564;
  ProgramSequence sequence2565;
  ProgramSequence sequence2566;
  ProgramSequenceChord sequenceChord2567;
  ProgramSequenceChord sequenceChord2568;
  ProgramSequenceChord sequenceChord2569;
  ProgramSequenceChord sequenceChord2570;
  ProgramSequenceChord sequenceChord2571;
  ProgramSequenceChord sequenceChord2572;
  ProgramSequenceChord sequenceChord2573;
  ProgramSequenceChord sequenceChord2574;
  ProgramSequenceChord sequenceChord2575;
  ProgramSequenceChord sequenceChord2576;
  ProgramSequenceBinding sequenceBinding2577;
  ProgramSequenceBinding sequenceBinding2578;
  ProgramSequenceBinding sequenceBinding2579;
  ProgramSequenceBinding sequenceBinding2580;
  ProgramSequenceBinding sequenceBinding2581;
  ProgramSequenceBinding sequenceBinding2582;
  ProgramSequenceBinding sequenceBinding2583;
  ProgramSequenceBinding sequenceBinding2584;
  ProgramSequenceBinding sequenceBinding2585;
  ProgramSequenceBinding sequenceBinding2586;
  ProgramSequenceBinding sequenceBinding2587;
  ProgramSequenceBinding sequenceBinding2588;
  ProgramSequenceBinding sequenceBinding2589;
  ProgramSequenceBinding sequenceBinding2590;
  ProgramSequenceBinding sequenceBinding2591;
  ProgramSequenceBinding sequenceBinding2592;
  ProgramSequenceBinding sequenceBinding2593;
  ProgramSequenceBinding sequenceBinding2594;
  ProgramSequenceBinding sequenceBinding2595;
  ProgramSequenceBinding sequenceBinding2596;
  ProgramSequenceBinding sequenceBinding2597;
  ProgramSequenceBinding sequenceBinding2598;
  ProgramSequenceBinding sequenceBinding2599;
  ProgramSequenceBinding sequenceBinding2600;
  ProgramSequenceBinding sequenceBinding2601;
  ProgramSequenceBinding sequenceBinding2602;
  ProgramSequenceBindingMeme sequenceBindingMeme2603;
  ProgramSequenceBindingMeme sequenceBindingMeme2604;
  ProgramSequenceBindingMeme sequenceBindingMeme2605;
  ProgramSequenceBindingMeme sequenceBindingMeme2606;
  ProgramSequenceBindingMeme sequenceBindingMeme2607;
  ProgramSequenceBindingMeme sequenceBindingMeme2608;
  ProgramSequenceBindingMeme sequenceBindingMeme2609;
  ProgramSequenceBindingMeme sequenceBindingMeme2610;
  ProgramSequenceBindingMeme sequenceBindingMeme2611;
  ProgramSequenceBindingMeme sequenceBindingMeme2612;
  ProgramSequenceBindingMeme sequenceBindingMeme2613;
  ProgramSequenceBindingMeme sequenceBindingMeme2614;
  ProgramSequenceBindingMeme sequenceBindingMeme2615;
  ProgramSequenceBindingMeme sequenceBindingMeme2616;
  ProgramSequenceBindingMeme sequenceBindingMeme2617;
  ProgramSequenceBindingMeme sequenceBindingMeme2618;
  ProgramSequenceBindingMeme sequenceBindingMeme2619;
  ProgramSequenceBindingMeme sequenceBindingMeme2620;
  ProgramSequenceBindingMeme sequenceBindingMeme2621;
  ProgramSequenceBindingMeme sequenceBindingMeme2622;
  ProgramSequenceBindingMeme sequenceBindingMeme2623;
  ProgramSequenceBindingMeme sequenceBindingMeme2624;
  ProgramSequenceBindingMeme sequenceBindingMeme2625;
  ProgramSequenceBindingMeme sequenceBindingMeme2626;
  ProgramSequenceBindingMeme sequenceBindingMeme2627;
  ProgramSequenceBindingMeme sequenceBindingMeme2628;
  Program program82;
  ProgramSequence sequence2629;
  ProgramSequence sequence2630;
  ProgramSequence sequence2631;
  ProgramSequenceChord sequenceChord2632;
  ProgramSequenceChord sequenceChord2633;
  ProgramSequenceChord sequenceChord2634;
  ProgramSequenceChord sequenceChord2635;
  ProgramSequenceChord sequenceChord2636;
  ProgramSequenceChord sequenceChord2637;
  ProgramSequenceBinding sequenceBinding2638;
  ProgramSequenceBinding sequenceBinding2639;
  ProgramSequenceBinding sequenceBinding2640;
  ProgramSequenceBinding sequenceBinding2641;
  ProgramSequenceBinding sequenceBinding2642;
  ProgramSequenceBinding sequenceBinding2643;
  ProgramSequenceBinding sequenceBinding2644;
  ProgramSequenceBinding sequenceBinding2645;
  ProgramSequenceBinding sequenceBinding2646;
  ProgramSequenceBinding sequenceBinding2647;
  ProgramSequenceBinding sequenceBinding2648;
  ProgramSequenceBinding sequenceBinding2649;
  ProgramSequenceBinding sequenceBinding2650;
  ProgramSequenceBinding sequenceBinding2651;
  ProgramSequenceBinding sequenceBinding2652;
  ProgramSequenceBinding sequenceBinding2653;
  ProgramSequenceBinding sequenceBinding2654;
  ProgramSequenceBinding sequenceBinding2655;
  ProgramSequenceBinding sequenceBinding2656;
  ProgramSequenceBinding sequenceBinding2657;
  ProgramSequenceBinding sequenceBinding2658;
  ProgramSequenceBinding sequenceBinding2659;
  ProgramSequenceBindingMeme sequenceBindingMeme2660;
  ProgramSequenceBindingMeme sequenceBindingMeme2661;
  ProgramSequenceBindingMeme sequenceBindingMeme2662;
  ProgramSequenceBindingMeme sequenceBindingMeme2663;
  ProgramSequenceBindingMeme sequenceBindingMeme2664;
  ProgramSequenceBindingMeme sequenceBindingMeme2665;
  ProgramSequenceBindingMeme sequenceBindingMeme2666;
  ProgramSequenceBindingMeme sequenceBindingMeme2667;
  ProgramSequenceBindingMeme sequenceBindingMeme2668;
  ProgramSequenceBindingMeme sequenceBindingMeme2669;
  ProgramSequenceBindingMeme sequenceBindingMeme2670;
  ProgramSequenceBindingMeme sequenceBindingMeme2671;
  ProgramSequenceBindingMeme sequenceBindingMeme2672;
  ProgramSequenceBindingMeme sequenceBindingMeme2673;
  ProgramSequenceBindingMeme sequenceBindingMeme2674;
  ProgramSequenceBindingMeme sequenceBindingMeme2675;
  ProgramSequenceBindingMeme sequenceBindingMeme2676;
  ProgramSequenceBindingMeme sequenceBindingMeme2677;
  ProgramSequenceBindingMeme sequenceBindingMeme2678;
  ProgramSequenceBindingMeme sequenceBindingMeme2679;
  ProgramSequenceBindingMeme sequenceBindingMeme2680;
  ProgramSequenceBindingMeme sequenceBindingMeme2681;
  Program program65;
  ProgramSequence sequence2682;
  ProgramSequenceChord sequenceChord2683;
  ProgramSequenceChord sequenceChord2684;
  ProgramSequenceChord sequenceChord2685;
  ProgramSequenceChord sequenceChord2686;
  ProgramSequenceChord sequenceChord2687;
  ProgramSequenceChord sequenceChord2688;
  ProgramSequenceChord sequenceChord2689;
  ProgramSequenceChord sequenceChord2690;
  ProgramSequenceBinding sequenceBinding2691;
  ProgramSequenceBinding sequenceBinding2692;
  ProgramSequenceBinding sequenceBinding2693;
  ProgramSequenceBinding sequenceBinding2694;
  ProgramSequenceBinding sequenceBinding2695;
  ProgramSequenceBinding sequenceBinding2696;
  ProgramSequenceBinding sequenceBinding2697;
  ProgramSequenceBinding sequenceBinding2698;
  ProgramSequenceBinding sequenceBinding2699;
  ProgramSequenceBinding sequenceBinding2700;
  ProgramSequenceBinding sequenceBinding2701;
  ProgramSequenceBinding sequenceBinding2702;
  ProgramSequenceBinding sequenceBinding2703;
  ProgramSequenceBinding sequenceBinding2704;
  ProgramSequenceBindingMeme sequenceBindingMeme2705;
  ProgramSequenceBindingMeme sequenceBindingMeme2706;
  ProgramSequenceBindingMeme sequenceBindingMeme2707;
  ProgramSequenceBindingMeme sequenceBindingMeme2708;
  ProgramSequenceBindingMeme sequenceBindingMeme2709;
  ProgramSequenceBindingMeme sequenceBindingMeme2710;
  ProgramSequenceBindingMeme sequenceBindingMeme2711;
  ProgramSequenceBindingMeme sequenceBindingMeme2712;
  ProgramSequenceBindingMeme sequenceBindingMeme2713;
  ProgramSequenceBindingMeme sequenceBindingMeme2714;
  ProgramSequenceBindingMeme sequenceBindingMeme2715;
  ProgramSequenceBindingMeme sequenceBindingMeme2716;
  ProgramSequenceBindingMeme sequenceBindingMeme2717;
  ProgramSequenceBindingMeme sequenceBindingMeme2718;
  Program program57;
  ProgramSequence sequence2719;
  ProgramSequence sequence2720;
  ProgramSequenceChord sequenceChord2721;
  ProgramSequenceChord sequenceChord2722;
  ProgramSequenceChord sequenceChord2723;
  ProgramSequenceChord sequenceChord2724;
  ProgramSequenceChord sequenceChord2725;
  ProgramSequenceBinding sequenceBinding2726;
  ProgramSequenceBinding sequenceBinding2727;
  ProgramSequenceBinding sequenceBinding2728;
  ProgramSequenceBinding sequenceBinding2729;
  ProgramSequenceBinding sequenceBinding2730;
  ProgramSequenceBinding sequenceBinding2731;
  ProgramSequenceBinding sequenceBinding2732;
  ProgramSequenceBinding sequenceBinding2733;
  ProgramSequenceBinding sequenceBinding2734;
  ProgramSequenceBinding sequenceBinding2735;
  ProgramSequenceBinding sequenceBinding2736;
  ProgramSequenceBinding sequenceBinding2737;
  ProgramSequenceBinding sequenceBinding2738;
  ProgramSequenceBinding sequenceBinding2739;
  ProgramSequenceBinding sequenceBinding2740;
  ProgramSequenceBinding sequenceBinding2741;
  ProgramSequenceBinding sequenceBinding2742;
  ProgramSequenceBinding sequenceBinding2743;
  ProgramSequenceBinding sequenceBinding2744;
  ProgramSequenceBinding sequenceBinding2745;
  ProgramSequenceBindingMeme sequenceBindingMeme2746;
  ProgramSequenceBindingMeme sequenceBindingMeme2747;
  ProgramSequenceBindingMeme sequenceBindingMeme2748;
  ProgramSequenceBindingMeme sequenceBindingMeme2749;
  ProgramSequenceBindingMeme sequenceBindingMeme2750;
  ProgramSequenceBindingMeme sequenceBindingMeme2751;
  ProgramSequenceBindingMeme sequenceBindingMeme2752;
  ProgramSequenceBindingMeme sequenceBindingMeme2753;
  ProgramSequenceBindingMeme sequenceBindingMeme2754;
  ProgramSequenceBindingMeme sequenceBindingMeme2755;
  ProgramSequenceBindingMeme sequenceBindingMeme2756;
  ProgramSequenceBindingMeme sequenceBindingMeme2757;
  ProgramSequenceBindingMeme sequenceBindingMeme2758;
  ProgramSequenceBindingMeme sequenceBindingMeme2759;
  ProgramSequenceBindingMeme sequenceBindingMeme2760;
  ProgramSequenceBindingMeme sequenceBindingMeme2761;
  ProgramSequenceBindingMeme sequenceBindingMeme2762;
  ProgramSequenceBindingMeme sequenceBindingMeme2763;
  ProgramSequenceBindingMeme sequenceBindingMeme2764;
  ProgramSequenceBindingMeme sequenceBindingMeme2765;
  Program program77;
  ProgramSequence sequence2766;
  ProgramSequenceChord sequenceChord2767;
  ProgramSequenceChord sequenceChord2768;
  ProgramSequenceChord sequenceChord2769;
  ProgramSequenceChord sequenceChord2770;
  ProgramSequenceBinding sequenceBinding2771;
  ProgramSequenceBinding sequenceBinding2772;
  ProgramSequenceBinding sequenceBinding2773;
  ProgramSequenceBinding sequenceBinding2774;
  ProgramSequenceBinding sequenceBinding2775;
  ProgramSequenceBinding sequenceBinding2776;
  ProgramSequenceBinding sequenceBinding2777;
  ProgramSequenceBinding sequenceBinding2778;
  ProgramSequenceBinding sequenceBinding2779;
  ProgramSequenceBinding sequenceBinding2780;
  ProgramSequenceBinding sequenceBinding2781;
  ProgramSequenceBinding sequenceBinding2782;
  ProgramSequenceBindingMeme sequenceBindingMeme2783;
  ProgramSequenceBindingMeme sequenceBindingMeme2784;
  ProgramSequenceBindingMeme sequenceBindingMeme2785;
  ProgramSequenceBindingMeme sequenceBindingMeme2786;
  ProgramSequenceBindingMeme sequenceBindingMeme2787;
  ProgramSequenceBindingMeme sequenceBindingMeme2788;
  ProgramSequenceBindingMeme sequenceBindingMeme2789;
  ProgramSequenceBindingMeme sequenceBindingMeme2790;
  ProgramSequenceBindingMeme sequenceBindingMeme2791;
  ProgramSequenceBindingMeme sequenceBindingMeme2792;
  ProgramSequenceBindingMeme sequenceBindingMeme2793;
  ProgramSequenceBindingMeme sequenceBindingMeme2794;
  Program program85;
  ProgramSequence sequence2795;
  ProgramSequence sequence2796;
  ProgramSequence sequence2797;
  ProgramSequence sequence2798;
  ProgramSequenceChord sequenceChord2799;
  ProgramSequenceChord sequenceChord2800;
  ProgramSequenceChord sequenceChord2801;
  ProgramSequenceChord sequenceChord2802;
  ProgramSequenceChord sequenceChord2803;
  ProgramSequenceChord sequenceChord2804;
  ProgramSequenceChord sequenceChord2805;
  ProgramSequenceChord sequenceChord2806;
  ProgramSequenceBinding sequenceBinding2807;
  ProgramSequenceBinding sequenceBinding2808;
  ProgramSequenceBinding sequenceBinding2809;
  ProgramSequenceBinding sequenceBinding2810;
  ProgramSequenceBinding sequenceBinding2811;
  ProgramSequenceBinding sequenceBinding2812;
  ProgramSequenceBinding sequenceBinding2813;
  ProgramSequenceBinding sequenceBinding2814;
  ProgramSequenceBinding sequenceBinding2815;
  ProgramSequenceBinding sequenceBinding2816;
  ProgramSequenceBinding sequenceBinding2817;
  ProgramSequenceBinding sequenceBinding2818;
  ProgramSequenceBinding sequenceBinding2819;
  ProgramSequenceBinding sequenceBinding2820;
  ProgramSequenceBinding sequenceBinding2821;
  ProgramSequenceBinding sequenceBinding2822;
  ProgramSequenceBinding sequenceBinding2823;
  ProgramSequenceBinding sequenceBinding2824;
  ProgramSequenceBinding sequenceBinding2825;
  ProgramSequenceBindingMeme sequenceBindingMeme2826;
  ProgramSequenceBindingMeme sequenceBindingMeme2827;
  ProgramSequenceBindingMeme sequenceBindingMeme2828;
  ProgramSequenceBindingMeme sequenceBindingMeme2829;
  ProgramSequenceBindingMeme sequenceBindingMeme2830;
  ProgramSequenceBindingMeme sequenceBindingMeme2831;
  ProgramSequenceBindingMeme sequenceBindingMeme2832;
  ProgramSequenceBindingMeme sequenceBindingMeme2833;
  ProgramSequenceBindingMeme sequenceBindingMeme2834;
  ProgramSequenceBindingMeme sequenceBindingMeme2835;
  ProgramSequenceBindingMeme sequenceBindingMeme2836;
  ProgramSequenceBindingMeme sequenceBindingMeme2837;
  ProgramSequenceBindingMeme sequenceBindingMeme2838;
  ProgramSequenceBindingMeme sequenceBindingMeme2839;
  ProgramSequenceBindingMeme sequenceBindingMeme2840;
  ProgramSequenceBindingMeme sequenceBindingMeme2841;
  ProgramSequenceBindingMeme sequenceBindingMeme2842;
  ProgramSequenceBindingMeme sequenceBindingMeme2843;
  ProgramSequenceBindingMeme sequenceBindingMeme2844;
  Program program53;
  ProgramSequence sequence2845;
  ProgramSequence sequence2846;
  ProgramSequence sequence2847;
  ProgramSequence sequence2848;
  ProgramSequenceChord sequenceChord2849;
  ProgramSequenceChord sequenceChord2850;
  ProgramSequenceChord sequenceChord2851;
  ProgramSequenceChord sequenceChord2852;
  ProgramSequenceChord sequenceChord2853;
  ProgramSequenceChord sequenceChord2854;
  ProgramSequenceChord sequenceChord2855;
  ProgramSequenceChord sequenceChord2856;
  ProgramSequenceChord sequenceChord2857;
  ProgramSequenceChord sequenceChord2858;
  ProgramSequenceChord sequenceChord2859;
  ProgramSequenceChord sequenceChord2860;
  ProgramSequenceChord sequenceChord2861;
  ProgramSequenceChord sequenceChord2862;
  ProgramSequenceChord sequenceChord2863;
  ProgramSequenceChord sequenceChord2864;
  ProgramSequenceBinding sequenceBinding2865;
  ProgramSequenceBinding sequenceBinding2866;
  ProgramSequenceBinding sequenceBinding2867;
  ProgramSequenceBinding sequenceBinding2868;
  ProgramSequenceBinding sequenceBinding2869;
  ProgramSequenceBinding sequenceBinding2870;
  ProgramSequenceBinding sequenceBinding2871;
  ProgramSequenceBinding sequenceBinding2872;
  ProgramSequenceBinding sequenceBinding2873;
  ProgramSequenceBinding sequenceBinding2874;
  ProgramSequenceBinding sequenceBinding2875;
  ProgramSequenceBinding sequenceBinding2876;
  ProgramSequenceBinding sequenceBinding2877;
  ProgramSequenceBinding sequenceBinding2878;
  ProgramSequenceBinding sequenceBinding2879;
  ProgramSequenceBinding sequenceBinding2880;
  ProgramSequenceBinding sequenceBinding2881;
  ProgramSequenceBinding sequenceBinding2882;
  ProgramSequenceBinding sequenceBinding2883;
  ProgramSequenceBinding sequenceBinding2884;
  ProgramSequenceBinding sequenceBinding2885;
  ProgramSequenceBinding sequenceBinding2886;
  ProgramSequenceBinding sequenceBinding2887;
  ProgramSequenceBinding sequenceBinding2888;
  ProgramSequenceBinding sequenceBinding2889;
  ProgramSequenceBinding sequenceBinding2890;
  ProgramSequenceBinding sequenceBinding2891;
  ProgramSequenceBinding sequenceBinding2892;
  ProgramSequenceBinding sequenceBinding2893;
  ProgramSequenceBindingMeme sequenceBindingMeme2894;
  ProgramSequenceBindingMeme sequenceBindingMeme2895;
  ProgramSequenceBindingMeme sequenceBindingMeme2896;
  ProgramSequenceBindingMeme sequenceBindingMeme2897;
  ProgramSequenceBindingMeme sequenceBindingMeme2898;
  ProgramSequenceBindingMeme sequenceBindingMeme2899;
  ProgramSequenceBindingMeme sequenceBindingMeme2900;
  ProgramSequenceBindingMeme sequenceBindingMeme2901;
  ProgramSequenceBindingMeme sequenceBindingMeme2902;
  ProgramSequenceBindingMeme sequenceBindingMeme2903;
  ProgramSequenceBindingMeme sequenceBindingMeme2904;
  ProgramSequenceBindingMeme sequenceBindingMeme2905;
  ProgramSequenceBindingMeme sequenceBindingMeme2906;
  ProgramSequenceBindingMeme sequenceBindingMeme2907;
  ProgramSequenceBindingMeme sequenceBindingMeme2908;
  ProgramSequenceBindingMeme sequenceBindingMeme2909;
  ProgramSequenceBindingMeme sequenceBindingMeme2910;
  ProgramSequenceBindingMeme sequenceBindingMeme2911;
  ProgramSequenceBindingMeme sequenceBindingMeme2912;
  ProgramSequenceBindingMeme sequenceBindingMeme2913;
  ProgramSequenceBindingMeme sequenceBindingMeme2914;
  ProgramSequenceBindingMeme sequenceBindingMeme2915;
  ProgramSequenceBindingMeme sequenceBindingMeme2916;
  ProgramSequenceBindingMeme sequenceBindingMeme2917;
  ProgramSequenceBindingMeme sequenceBindingMeme2918;
  ProgramSequenceBindingMeme sequenceBindingMeme2919;
  ProgramSequenceBindingMeme sequenceBindingMeme2920;
  ProgramSequenceBindingMeme sequenceBindingMeme2921;
  ProgramSequenceBindingMeme sequenceBindingMeme2922;
  Program program54;
  ProgramSequence sequence2923;
  ProgramSequence sequence2924;
  ProgramSequenceChord sequenceChord2925;
  ProgramSequenceChord sequenceChord2926;
  ProgramSequenceChord sequenceChord2927;
  ProgramSequenceChord sequenceChord2928;
  ProgramSequenceBinding sequenceBinding2929;
  ProgramSequenceBinding sequenceBinding2930;
  ProgramSequenceBinding sequenceBinding2931;
  ProgramSequenceBinding sequenceBinding2932;
  ProgramSequenceBinding sequenceBinding2933;
  ProgramSequenceBinding sequenceBinding2934;
  ProgramSequenceBinding sequenceBinding2935;
  ProgramSequenceBinding sequenceBinding2936;
  ProgramSequenceBinding sequenceBinding2937;
  ProgramSequenceBinding sequenceBinding2938;
  ProgramSequenceBinding sequenceBinding2939;
  ProgramSequenceBinding sequenceBinding2940;
  ProgramSequenceBinding sequenceBinding2941;
  ProgramSequenceBinding sequenceBinding2942;
  ProgramSequenceBinding sequenceBinding2943;
  ProgramSequenceBinding sequenceBinding2944;
  ProgramSequenceBinding sequenceBinding2945;
  ProgramSequenceBinding sequenceBinding2946;
  ProgramSequenceBinding sequenceBinding2947;
  ProgramSequenceBinding sequenceBinding2948;
  ProgramSequenceBinding sequenceBinding2949;
  ProgramSequenceBinding sequenceBinding2950;
  ProgramSequenceBinding sequenceBinding2951;
  ProgramSequenceBinding sequenceBinding2952;
  ProgramSequenceBinding sequenceBinding2953;
  ProgramSequenceBinding sequenceBinding2954;
  ProgramSequenceBinding sequenceBinding2955;
  ProgramSequenceBindingMeme sequenceBindingMeme2956;
  ProgramSequenceBindingMeme sequenceBindingMeme2957;
  ProgramSequenceBindingMeme sequenceBindingMeme2958;
  ProgramSequenceBindingMeme sequenceBindingMeme2959;
  ProgramSequenceBindingMeme sequenceBindingMeme2960;
  ProgramSequenceBindingMeme sequenceBindingMeme2961;
  ProgramSequenceBindingMeme sequenceBindingMeme2962;
  ProgramSequenceBindingMeme sequenceBindingMeme2963;
  ProgramSequenceBindingMeme sequenceBindingMeme2964;
  ProgramSequenceBindingMeme sequenceBindingMeme2965;
  ProgramSequenceBindingMeme sequenceBindingMeme2966;
  ProgramSequenceBindingMeme sequenceBindingMeme2967;
  ProgramSequenceBindingMeme sequenceBindingMeme2968;
  ProgramSequenceBindingMeme sequenceBindingMeme2969;
  ProgramSequenceBindingMeme sequenceBindingMeme2970;
  ProgramSequenceBindingMeme sequenceBindingMeme2971;
  ProgramSequenceBindingMeme sequenceBindingMeme2972;
  ProgramSequenceBindingMeme sequenceBindingMeme2973;
  ProgramSequenceBindingMeme sequenceBindingMeme2974;
  ProgramSequenceBindingMeme sequenceBindingMeme2975;
  ProgramSequenceBindingMeme sequenceBindingMeme2976;
  ProgramSequenceBindingMeme sequenceBindingMeme2977;
  ProgramSequenceBindingMeme sequenceBindingMeme2978;
  ProgramSequenceBindingMeme sequenceBindingMeme2979;
  ProgramSequenceBindingMeme sequenceBindingMeme2980;
  ProgramSequenceBindingMeme sequenceBindingMeme2981;
  ProgramSequenceBindingMeme sequenceBindingMeme2982;
  Program program50;
  ProgramSequence sequence2983;
  ProgramSequence sequence2984;
  ProgramSequence sequence2985;
  ProgramSequence sequence2986;
  ProgramSequence sequence2987;
  ProgramSequence sequence2988;
  ProgramSequenceChord sequenceChord2989;
  ProgramSequenceChord sequenceChord2990;
  ProgramSequenceChord sequenceChord2991;
  ProgramSequenceChord sequenceChord2992;
  ProgramSequenceChord sequenceChord2993;
  ProgramSequenceChord sequenceChord2994;
  ProgramSequenceChord sequenceChord2995;
  ProgramSequenceChord sequenceChord2996;
  ProgramSequenceChord sequenceChord2997;
  ProgramSequenceChord sequenceChord2998;
  ProgramSequenceChord sequenceChord2999;
  ProgramSequenceBinding sequenceBinding3000;
  ProgramSequenceBinding sequenceBinding3001;
  ProgramSequenceBinding sequenceBinding3002;
  ProgramSequenceBinding sequenceBinding3003;
  ProgramSequenceBinding sequenceBinding3004;
  ProgramSequenceBinding sequenceBinding3005;
  ProgramSequenceBinding sequenceBinding3006;
  ProgramSequenceBinding sequenceBinding3007;
  ProgramSequenceBinding sequenceBinding3008;
  ProgramSequenceBinding sequenceBinding3009;
  ProgramSequenceBinding sequenceBinding3010;
  ProgramSequenceBinding sequenceBinding3011;
  ProgramSequenceBinding sequenceBinding3012;
  ProgramSequenceBinding sequenceBinding3013;
  ProgramSequenceBinding sequenceBinding3014;
  ProgramSequenceBinding sequenceBinding3015;
  ProgramSequenceBinding sequenceBinding3016;
  ProgramSequenceBinding sequenceBinding3017;
  ProgramSequenceBinding sequenceBinding3018;
  ProgramSequenceBinding sequenceBinding3019;
  ProgramSequenceBinding sequenceBinding3020;
  ProgramSequenceBinding sequenceBinding3021;
  ProgramSequenceBinding sequenceBinding3022;
  ProgramSequenceBinding sequenceBinding3023;
  ProgramSequenceBinding sequenceBinding3024;
  ProgramSequenceBinding sequenceBinding3025;
  ProgramSequenceBinding sequenceBinding3026;
  ProgramSequenceBinding sequenceBinding3027;
  ProgramSequenceBinding sequenceBinding3028;
  ProgramSequenceBinding sequenceBinding3029;
  ProgramSequenceBindingMeme sequenceBindingMeme3030;
  ProgramSequenceBindingMeme sequenceBindingMeme3031;
  ProgramSequenceBindingMeme sequenceBindingMeme3032;
  ProgramSequenceBindingMeme sequenceBindingMeme3033;
  ProgramSequenceBindingMeme sequenceBindingMeme3034;
  ProgramSequenceBindingMeme sequenceBindingMeme3035;
  ProgramSequenceBindingMeme sequenceBindingMeme3036;
  ProgramSequenceBindingMeme sequenceBindingMeme3037;
  ProgramSequenceBindingMeme sequenceBindingMeme3038;
  ProgramSequenceBindingMeme sequenceBindingMeme3039;
  ProgramSequenceBindingMeme sequenceBindingMeme3040;
  ProgramSequenceBindingMeme sequenceBindingMeme3041;
  ProgramSequenceBindingMeme sequenceBindingMeme3042;
  ProgramSequenceBindingMeme sequenceBindingMeme3043;
  ProgramSequenceBindingMeme sequenceBindingMeme3044;
  ProgramSequenceBindingMeme sequenceBindingMeme3045;
  ProgramSequenceBindingMeme sequenceBindingMeme3046;
  ProgramSequenceBindingMeme sequenceBindingMeme3047;
  ProgramSequenceBindingMeme sequenceBindingMeme3048;
  ProgramSequenceBindingMeme sequenceBindingMeme3049;
  ProgramSequenceBindingMeme sequenceBindingMeme3050;
  ProgramSequenceBindingMeme sequenceBindingMeme3051;
  ProgramSequenceBindingMeme sequenceBindingMeme3052;
  ProgramSequenceBindingMeme sequenceBindingMeme3053;
  ProgramSequenceBindingMeme sequenceBindingMeme3054;
  ProgramSequenceBindingMeme sequenceBindingMeme3055;
  ProgramSequenceBindingMeme sequenceBindingMeme3056;
  ProgramSequenceBindingMeme sequenceBindingMeme3057;
  ProgramSequenceBindingMeme sequenceBindingMeme3058;
  ProgramSequenceBindingMeme sequenceBindingMeme3059;
  Program program62;
  ProgramSequence sequence3060;
  ProgramSequenceChord sequenceChord3061;
  ProgramSequenceChord sequenceChord3062;
  ProgramSequenceChord sequenceChord3063;
  ProgramSequenceChord sequenceChord3064;
  ProgramSequenceChord sequenceChord3065;
  ProgramSequenceBinding sequenceBinding3066;
  ProgramSequenceBinding sequenceBinding3067;
  ProgramSequenceBinding sequenceBinding3068;
  ProgramSequenceBinding sequenceBinding3069;
  ProgramSequenceBinding sequenceBinding3070;
  ProgramSequenceBinding sequenceBinding3071;
  ProgramSequenceBinding sequenceBinding3072;
  ProgramSequenceBinding sequenceBinding3073;
  ProgramSequenceBindingMeme sequenceBindingMeme3074;
  ProgramSequenceBindingMeme sequenceBindingMeme3075;
  ProgramSequenceBindingMeme sequenceBindingMeme3076;
  ProgramSequenceBindingMeme sequenceBindingMeme3077;
  ProgramSequenceBindingMeme sequenceBindingMeme3078;
  ProgramSequenceBindingMeme sequenceBindingMeme3079;
  ProgramSequenceBindingMeme sequenceBindingMeme3080;
  ProgramSequenceBindingMeme sequenceBindingMeme3081;
  Program program83;
  ProgramSequence sequence3082;
  ProgramSequenceChord sequenceChord3083;
  ProgramSequenceBinding sequenceBinding3084;
  ProgramSequenceBinding sequenceBinding3085;
  ProgramSequenceBinding sequenceBinding3086;
  ProgramSequenceBinding sequenceBinding3087;
  ProgramSequenceBinding sequenceBinding3088;
  ProgramSequenceBinding sequenceBinding3089;
  ProgramSequenceBinding sequenceBinding3090;
  ProgramSequenceBinding sequenceBinding3091;
  ProgramSequenceBinding sequenceBinding3092;
  ProgramSequenceBindingMeme sequenceBindingMeme3093;
  ProgramSequenceBindingMeme sequenceBindingMeme3094;
  ProgramSequenceBindingMeme sequenceBindingMeme3095;
  ProgramSequenceBindingMeme sequenceBindingMeme3096;
  ProgramSequenceBindingMeme sequenceBindingMeme3097;
  ProgramSequenceBindingMeme sequenceBindingMeme3098;
  ProgramSequenceBindingMeme sequenceBindingMeme3099;
  ProgramSequenceBindingMeme sequenceBindingMeme3100;
  ProgramSequenceBindingMeme sequenceBindingMeme3101;
  Program program78;
  ProgramSequence sequence3102;
  ProgramSequence sequence3103;
  ProgramSequenceChord sequenceChord3104;
  ProgramSequenceChord sequenceChord3105;
  ProgramSequenceChord sequenceChord3106;
  ProgramSequenceChord sequenceChord3107;
  ProgramSequenceChord sequenceChord3108;
  ProgramSequenceChord sequenceChord3109;
  ProgramSequenceChord sequenceChord3110;
  ProgramSequenceBinding sequenceBinding3111;
  ProgramSequenceBinding sequenceBinding3112;
  ProgramSequenceBinding sequenceBinding3113;
  ProgramSequenceBinding sequenceBinding3114;
  ProgramSequenceBinding sequenceBinding3115;
  ProgramSequenceBinding sequenceBinding3116;
  ProgramSequenceBinding sequenceBinding3117;
  ProgramSequenceBinding sequenceBinding3118;
  ProgramSequenceBinding sequenceBinding3119;
  ProgramSequenceBinding sequenceBinding3120;
  ProgramSequenceBinding sequenceBinding3121;
  ProgramSequenceBinding sequenceBinding3122;
  ProgramSequenceBinding sequenceBinding3123;
  ProgramSequenceBinding sequenceBinding3124;
  ProgramSequenceBinding sequenceBinding3125;
  ProgramSequenceBinding sequenceBinding3126;
  ProgramSequenceBinding sequenceBinding3127;
  ProgramSequenceBinding sequenceBinding3128;
  ProgramSequenceBinding sequenceBinding3129;
  ProgramSequenceBindingMeme sequenceBindingMeme3130;
  ProgramSequenceBindingMeme sequenceBindingMeme3131;
  ProgramSequenceBindingMeme sequenceBindingMeme3132;
  ProgramSequenceBindingMeme sequenceBindingMeme3133;
  ProgramSequenceBindingMeme sequenceBindingMeme3134;
  ProgramSequenceBindingMeme sequenceBindingMeme3135;
  ProgramSequenceBindingMeme sequenceBindingMeme3136;
  ProgramSequenceBindingMeme sequenceBindingMeme3137;
  ProgramSequenceBindingMeme sequenceBindingMeme3138;
  ProgramSequenceBindingMeme sequenceBindingMeme3139;
  ProgramSequenceBindingMeme sequenceBindingMeme3140;
  ProgramSequenceBindingMeme sequenceBindingMeme3141;
  ProgramSequenceBindingMeme sequenceBindingMeme3142;
  ProgramSequenceBindingMeme sequenceBindingMeme3143;
  ProgramSequenceBindingMeme sequenceBindingMeme3144;
  ProgramSequenceBindingMeme sequenceBindingMeme3145;
  ProgramSequenceBindingMeme sequenceBindingMeme3146;
  ProgramSequenceBindingMeme sequenceBindingMeme3147;
  ProgramSequenceBindingMeme sequenceBindingMeme3148;
  Program program90;
  ProgramSequence sequence3149;
  ProgramSequenceChord sequenceChord3150;
  ProgramSequenceChord sequenceChord3151;
  ProgramSequenceChord sequenceChord3152;
  ProgramSequenceChord sequenceChord3153;
  ProgramSequenceBinding sequenceBinding3154;
  ProgramSequenceBinding sequenceBinding3155;
  ProgramSequenceBinding sequenceBinding3156;
  ProgramSequenceBinding sequenceBinding3157;
  ProgramSequenceBinding sequenceBinding3158;
  ProgramSequenceBinding sequenceBinding3159;
  ProgramSequenceBinding sequenceBinding3160;
  ProgramSequenceBinding sequenceBinding3161;
  ProgramSequenceBindingMeme sequenceBindingMeme3162;
  ProgramSequenceBindingMeme sequenceBindingMeme3163;
  ProgramSequenceBindingMeme sequenceBindingMeme3164;
  ProgramSequenceBindingMeme sequenceBindingMeme3165;
  ProgramSequenceBindingMeme sequenceBindingMeme3166;
  ProgramSequenceBindingMeme sequenceBindingMeme3167;
  ProgramSequenceBindingMeme sequenceBindingMeme3168;
  ProgramSequenceBindingMeme sequenceBindingMeme3169;
  Program program75;
  ProgramVoice voice3170;
  ProgramVoice voice3171;
  ProgramVoice voice3172;
  ProgramVoice voice3173;
  ProgramVoiceTrack track3174;
  ProgramVoiceTrack track3175;
  ProgramVoiceTrack track3176;
  ProgramVoiceTrack track3177;
  ProgramVoiceTrack track3178;
  ProgramVoiceTrack track3179;
  ProgramSequence sequence3180;
  ProgramSequenceBinding sequenceBinding3181;
  ProgramSequencePattern pattern3182;
  ProgramSequencePattern pattern3183;
  ProgramSequencePattern pattern3184;
  ProgramSequencePattern pattern3185;
  ProgramSequencePattern pattern3186;
  ProgramSequencePattern pattern3187;
  ProgramSequencePattern pattern3188;
  ProgramSequencePattern pattern3189;
  ProgramSequencePattern pattern3190;
  ProgramSequencePattern pattern3191;
  ProgramSequencePattern pattern3192;
  ProgramSequencePattern pattern3193;
  ProgramSequencePattern pattern3194;
  ProgramSequencePattern pattern3195;
  ProgramSequencePattern pattern3196;
  ProgramSequencePattern pattern3197;
  ProgramSequencePattern pattern3198;
  ProgramSequencePattern pattern3199;
  ProgramSequencePattern pattern3200;
  ProgramSequencePattern pattern3201;
  ProgramSequencePattern pattern3202;
  ProgramSequencePattern pattern3203;
  ProgramSequencePattern pattern3204;
  ProgramSequencePattern pattern3205;
  ProgramSequencePatternEvent event3206;
  ProgramSequencePatternEvent event3207;
  ProgramSequencePatternEvent event3208;
  ProgramSequencePatternEvent event3209;
  ProgramSequencePatternEvent event3210;
  ProgramSequencePatternEvent event3211;
  ProgramSequencePatternEvent event3212;
  ProgramSequencePatternEvent event3213;
  ProgramSequencePatternEvent event3214;
  ProgramSequencePatternEvent event3215;
  ProgramSequencePatternEvent event3216;
  ProgramSequencePatternEvent event3217;
  ProgramSequencePatternEvent event3218;
  ProgramSequencePatternEvent event3219;
  ProgramSequencePatternEvent event3220;
  ProgramSequencePatternEvent event3221;
  ProgramSequencePatternEvent event3222;
  ProgramSequencePatternEvent event3223;
  ProgramSequencePatternEvent event3224;
  ProgramSequencePatternEvent event3225;
  ProgramSequencePatternEvent event3226;
  ProgramSequencePatternEvent event3227;
  ProgramSequencePatternEvent event3228;
  ProgramSequencePatternEvent event3229;
  ProgramSequencePatternEvent event3230;
  ProgramSequencePatternEvent event3231;
  ProgramSequencePatternEvent event3232;
  ProgramSequencePatternEvent event3233;
  ProgramSequencePatternEvent event3234;
  ProgramSequencePatternEvent event3235;
  ProgramSequencePatternEvent event3236;
  ProgramSequencePatternEvent event3237;
  ProgramSequencePatternEvent event3238;
  ProgramSequencePatternEvent event3239;
  ProgramSequencePatternEvent event3240;
  ProgramSequencePatternEvent event3241;
  ProgramSequencePatternEvent event3242;
  ProgramSequencePatternEvent event3243;
  ProgramSequencePatternEvent event3244;
  ProgramSequencePatternEvent event3245;
  ProgramSequencePatternEvent event3246;
  ProgramSequencePatternEvent event3247;
  ProgramSequencePatternEvent event3248;
  ProgramSequencePatternEvent event3249;
  ProgramSequencePatternEvent event3250;
  ProgramSequencePatternEvent event3251;
  ProgramSequencePatternEvent event3252;
  ProgramSequencePatternEvent event3253;
  ProgramSequencePatternEvent event3254;
  ProgramSequencePatternEvent event3255;
  ProgramSequencePatternEvent event3256;
  ProgramSequencePatternEvent event3257;
  ProgramSequencePatternEvent event3258;
  ProgramSequencePatternEvent event3259;
  ProgramSequencePatternEvent event3260;
  ProgramSequencePatternEvent event3261;
  ProgramSequencePatternEvent event3262;
  ProgramSequencePatternEvent event3263;
  ProgramSequencePatternEvent event3264;
  ProgramSequencePatternEvent event3265;
  ProgramSequencePatternEvent event3266;
  ProgramSequencePatternEvent event3267;
  ProgramSequencePatternEvent event3268;
  ProgramSequencePatternEvent event3269;
  ProgramSequencePatternEvent event3270;
  ProgramSequencePatternEvent event3271;
  ProgramSequencePatternEvent event3272;
  ProgramSequencePatternEvent event3273;
  ProgramSequencePatternEvent event3274;
  ProgramSequencePatternEvent event3275;
  ProgramSequencePatternEvent event3276;
  ProgramSequencePatternEvent event3277;
  ProgramSequencePatternEvent event3278;
  ProgramSequencePatternEvent event3279;
  ProgramSequencePatternEvent event3280;
  ProgramSequencePatternEvent event3281;
  ProgramSequencePatternEvent event3282;
  ProgramSequencePatternEvent event3283;
  ProgramSequencePatternEvent event3284;
  ProgramSequencePatternEvent event3285;
  ProgramSequencePatternEvent event3286;
  ProgramSequencePatternEvent event3287;
  ProgramSequencePatternEvent event3288;
  ProgramSequencePatternEvent event3289;
  ProgramSequencePatternEvent event3290;
  ProgramSequencePatternEvent event3291;
  ProgramSequencePatternEvent event3292;
  ProgramSequencePatternEvent event3293;
  ProgramSequencePatternEvent event3294;
  ProgramSequencePatternEvent event3295;
  ProgramSequencePatternEvent event3296;
  ProgramSequencePatternEvent event3297;
  ProgramSequencePatternEvent event3298;
  ProgramSequencePatternEvent event3299;
  ProgramSequencePatternEvent event3300;
  ProgramSequencePatternEvent event3301;
  ProgramSequencePatternEvent event3302;
  ProgramSequencePatternEvent event3303;
  ProgramSequencePatternEvent event3304;
  ProgramSequencePatternEvent event3305;
  ProgramSequencePatternEvent event3306;
  ProgramSequencePatternEvent event3307;
  ProgramSequencePatternEvent event3308;
  ProgramSequencePatternEvent event3309;
  ProgramSequencePatternEvent event3310;
  Program program29;
  ProgramVoice voice3311;
  ProgramVoice voice3312;
  ProgramVoice voice3313;
  ProgramVoice voice3314;
  ProgramVoiceTrack track3315;
  ProgramVoiceTrack track3316;
  ProgramVoiceTrack track3317;
  ProgramVoiceTrack track3318;
  ProgramVoiceTrack track3319;
  ProgramVoiceTrack track3320;
  ProgramSequence sequence3321;
  ProgramSequenceBinding sequenceBinding3322;
  ProgramSequencePattern pattern3323;
  ProgramSequencePattern pattern3324;
  ProgramSequencePattern pattern3325;
  ProgramSequencePattern pattern3326;
  ProgramSequencePattern pattern3327;
  ProgramSequencePattern pattern3328;
  ProgramSequencePattern pattern3329;
  ProgramSequencePattern pattern3330;
  ProgramSequencePattern pattern3331;
  ProgramSequencePattern pattern3332;
  ProgramSequencePattern pattern3333;
  ProgramSequencePattern pattern3334;
  ProgramSequencePattern pattern3335;
  ProgramSequencePattern pattern3336;
  ProgramSequencePattern pattern3337;
  ProgramSequencePattern pattern3338;
  ProgramSequencePattern pattern3339;
  ProgramSequencePattern pattern3340;
  ProgramSequencePattern pattern3341;
  ProgramSequencePattern pattern3342;
  ProgramSequencePattern pattern3343;
  ProgramSequencePattern pattern3344;
  ProgramSequencePattern pattern3345;
  ProgramSequencePattern pattern3346;
  ProgramSequencePattern pattern3347;
  ProgramSequencePattern pattern3348;
  ProgramSequencePattern pattern3349;
  ProgramSequencePattern pattern3350;
  ProgramSequencePattern pattern3351;
  ProgramSequencePattern pattern3352;
  ProgramSequencePattern pattern3353;
  ProgramSequencePattern pattern3354;
  ProgramSequencePattern pattern3355;
  ProgramSequencePattern pattern3356;
  ProgramSequencePattern pattern3357;
  ProgramSequencePattern pattern3358;
  ProgramSequencePattern pattern3359;
  ProgramSequencePattern pattern3360;
  ProgramSequencePattern pattern3361;
  ProgramSequencePattern pattern3362;
  ProgramSequencePattern pattern3363;
  ProgramSequencePattern pattern3364;
  ProgramSequencePattern pattern3365;
  ProgramSequencePattern pattern3366;
  ProgramSequencePattern pattern3367;
  ProgramSequencePattern pattern3368;
  ProgramSequencePattern pattern3369;
  ProgramSequencePattern pattern3370;
  ProgramSequencePattern pattern3371;
  ProgramSequencePattern pattern3372;
  ProgramSequencePattern pattern3373;
  ProgramSequencePattern pattern3374;
  ProgramSequencePattern pattern3375;
  ProgramSequencePattern pattern3376;
  ProgramSequencePattern pattern3377;
  ProgramSequencePattern pattern3378;
  ProgramSequencePatternEvent event3379;
  ProgramSequencePatternEvent event3380;
  ProgramSequencePatternEvent event3381;
  ProgramSequencePatternEvent event3382;
  ProgramSequencePatternEvent event3383;
  ProgramSequencePatternEvent event3384;
  ProgramSequencePatternEvent event3385;
  ProgramSequencePatternEvent event3386;
  ProgramSequencePatternEvent event3387;
  ProgramSequencePatternEvent event3388;
  ProgramSequencePatternEvent event3389;
  ProgramSequencePatternEvent event3390;
  ProgramSequencePatternEvent event3391;
  ProgramSequencePatternEvent event3392;
  ProgramSequencePatternEvent event3393;
  ProgramSequencePatternEvent event3394;
  ProgramSequencePatternEvent event3395;
  ProgramSequencePatternEvent event3396;
  ProgramSequencePatternEvent event3397;
  ProgramSequencePatternEvent event3398;
  ProgramSequencePatternEvent event3399;
  ProgramSequencePatternEvent event3400;
  ProgramSequencePatternEvent event3401;
  ProgramSequencePatternEvent event3402;
  ProgramSequencePatternEvent event3403;
  ProgramSequencePatternEvent event3404;
  ProgramSequencePatternEvent event3405;
  ProgramSequencePatternEvent event3406;
  ProgramSequencePatternEvent event3407;
  ProgramSequencePatternEvent event3408;
  ProgramSequencePatternEvent event3409;
  ProgramSequencePatternEvent event3410;
  ProgramSequencePatternEvent event3411;
  ProgramSequencePatternEvent event3412;
  ProgramSequencePatternEvent event3413;
  ProgramSequencePatternEvent event3414;
  ProgramSequencePatternEvent event3415;
  ProgramSequencePatternEvent event3416;
  ProgramSequencePatternEvent event3417;
  ProgramSequencePatternEvent event3418;
  ProgramSequencePatternEvent event3419;
  ProgramSequencePatternEvent event3420;
  ProgramSequencePatternEvent event3421;
  ProgramSequencePatternEvent event3422;
  ProgramSequencePatternEvent event3423;
  ProgramSequencePatternEvent event3424;
  ProgramSequencePatternEvent event3425;
  ProgramSequencePatternEvent event3426;
  ProgramSequencePatternEvent event3427;
  ProgramSequencePatternEvent event3428;
  ProgramSequencePatternEvent event3429;
  ProgramSequencePatternEvent event3430;
  ProgramSequencePatternEvent event3431;
  ProgramSequencePatternEvent event3432;
  ProgramSequencePatternEvent event3433;
  ProgramSequencePatternEvent event3434;
  ProgramSequencePatternEvent event3435;
  ProgramSequencePatternEvent event3436;
  ProgramSequencePatternEvent event3437;
  ProgramSequencePatternEvent event3438;
  ProgramSequencePatternEvent event3439;
  ProgramSequencePatternEvent event3440;
  ProgramSequencePatternEvent event3441;
  ProgramSequencePatternEvent event3442;
  ProgramSequencePatternEvent event3443;
  ProgramSequencePatternEvent event3444;
  ProgramSequencePatternEvent event3445;
  ProgramSequencePatternEvent event3446;
  ProgramSequencePatternEvent event3447;
  ProgramSequencePatternEvent event3448;
  ProgramSequencePatternEvent event3449;
  ProgramSequencePatternEvent event3450;
  ProgramSequencePatternEvent event3451;
  ProgramSequencePatternEvent event3452;
  ProgramSequencePatternEvent event3453;
  ProgramSequencePatternEvent event3454;
  ProgramSequencePatternEvent event3455;
  ProgramSequencePatternEvent event3456;
  ProgramSequencePatternEvent event3457;
  ProgramSequencePatternEvent event3458;
  ProgramSequencePatternEvent event3459;
  ProgramSequencePatternEvent event3460;
  ProgramSequencePatternEvent event3461;
  ProgramSequencePatternEvent event3462;
  ProgramSequencePatternEvent event3463;
  ProgramSequencePatternEvent event3464;
  ProgramSequencePatternEvent event3465;
  ProgramSequencePatternEvent event3466;
  ProgramSequencePatternEvent event3467;
  ProgramSequencePatternEvent event3468;
  ProgramSequencePatternEvent event3469;
  ProgramSequencePatternEvent event3470;
  ProgramSequencePatternEvent event3471;
  ProgramSequencePatternEvent event3472;
  ProgramSequencePatternEvent event3473;
  ProgramSequencePatternEvent event3474;
  ProgramSequencePatternEvent event3475;
  ProgramSequencePatternEvent event3476;
  ProgramSequencePatternEvent event3477;
  ProgramSequencePatternEvent event3478;
  ProgramSequencePatternEvent event3479;
  ProgramSequencePatternEvent event3480;
  ProgramSequencePatternEvent event3481;
  ProgramSequencePatternEvent event3482;
  ProgramSequencePatternEvent event3483;
  ProgramSequencePatternEvent event3484;
  ProgramSequencePatternEvent event3485;
  ProgramSequencePatternEvent event3486;
  ProgramSequencePatternEvent event3487;
  ProgramSequencePatternEvent event3488;
  ProgramSequencePatternEvent event3489;
  ProgramSequencePatternEvent event3490;
  ProgramSequencePatternEvent event3491;
  ProgramSequencePatternEvent event3492;
  ProgramSequencePatternEvent event3493;
  ProgramSequencePatternEvent event3494;
  ProgramSequencePatternEvent event3495;
  ProgramSequencePatternEvent event3496;
  ProgramSequencePatternEvent event3497;
  ProgramSequencePatternEvent event3498;
  ProgramSequencePatternEvent event3499;
  ProgramSequencePatternEvent event3500;
  ProgramSequencePatternEvent event3501;
  ProgramSequencePatternEvent event3502;
  ProgramSequencePatternEvent event3503;
  ProgramSequencePatternEvent event3504;
  ProgramSequencePatternEvent event3505;
  ProgramSequencePatternEvent event3506;
  ProgramSequencePatternEvent event3507;
  ProgramSequencePatternEvent event3508;
  ProgramSequencePatternEvent event3509;
  ProgramSequencePatternEvent event3510;
  ProgramSequencePatternEvent event3511;
  ProgramSequencePatternEvent event3512;
  ProgramSequencePatternEvent event3513;
  ProgramSequencePatternEvent event3514;
  ProgramSequencePatternEvent event3515;
  ProgramSequencePatternEvent event3516;
  ProgramSequencePatternEvent event3517;
  ProgramSequencePatternEvent event3518;
  ProgramSequencePatternEvent event3519;
  ProgramSequencePatternEvent event3520;
  ProgramSequencePatternEvent event3521;
  ProgramSequencePatternEvent event3522;
  ProgramSequencePatternEvent event3523;
  ProgramSequencePatternEvent event3524;
  ProgramSequencePatternEvent event3525;
  ProgramSequencePatternEvent event3526;
  ProgramSequencePatternEvent event3527;
  ProgramSequencePatternEvent event3528;
  ProgramSequencePatternEvent event3529;
  ProgramSequencePatternEvent event3530;
  ProgramSequencePatternEvent event3531;
  ProgramSequencePatternEvent event3532;
  ProgramSequencePatternEvent event3533;
  ProgramSequencePatternEvent event3534;
  ProgramSequencePatternEvent event3535;
  ProgramSequencePatternEvent event3536;
  ProgramSequencePatternEvent event3537;
  ProgramSequencePatternEvent event3538;
  ProgramSequencePatternEvent event3539;
  ProgramSequencePatternEvent event3540;
  ProgramSequencePatternEvent event3541;
  ProgramSequencePatternEvent event3542;
  ProgramSequencePatternEvent event3543;
  ProgramSequencePatternEvent event3544;
  ProgramSequencePatternEvent event3545;
  ProgramSequencePatternEvent event3546;
  ProgramSequencePatternEvent event3547;
  ProgramSequencePatternEvent event3548;
  ProgramSequencePatternEvent event3549;
  ProgramSequencePatternEvent event3550;
  ProgramSequencePatternEvent event3551;
  ProgramSequencePatternEvent event3552;
  ProgramSequencePatternEvent event3553;
  ProgramSequencePatternEvent event3554;
  ProgramSequencePatternEvent event3555;
  ProgramSequencePatternEvent event3556;
  ProgramSequencePatternEvent event3557;
  ProgramSequencePatternEvent event3558;
  ProgramSequencePatternEvent event3559;
  ProgramSequencePatternEvent event3560;
  ProgramSequencePatternEvent event3561;
  ProgramSequencePatternEvent event3562;
  ProgramSequencePatternEvent event3563;
  ProgramSequencePatternEvent event3564;
  ProgramSequencePatternEvent event3565;
  ProgramSequencePatternEvent event3566;
  ProgramSequencePatternEvent event3567;
  ProgramSequencePatternEvent event3568;
  ProgramSequencePatternEvent event3569;
  ProgramSequencePatternEvent event3570;
  ProgramSequencePatternEvent event3571;
  ProgramSequencePatternEvent event3572;
  ProgramSequencePatternEvent event3573;
  ProgramSequencePatternEvent event3574;
  ProgramSequencePatternEvent event3575;
  ProgramSequencePatternEvent event3576;
  ProgramSequencePatternEvent event3577;
  ProgramSequencePatternEvent event3578;
  ProgramSequencePatternEvent event3579;
  ProgramSequencePatternEvent event3580;
  ProgramSequencePatternEvent event3581;
  ProgramSequencePatternEvent event3582;
  ProgramSequencePatternEvent event3583;
  ProgramSequencePatternEvent event3584;
  ProgramSequencePatternEvent event3585;
  ProgramSequencePatternEvent event3586;
  ProgramSequencePatternEvent event3587;
  ProgramSequencePatternEvent event3588;
  ProgramSequencePatternEvent event3589;
  ProgramSequencePatternEvent event3590;
  ProgramSequencePatternEvent event3591;
  ProgramSequencePatternEvent event3592;
  ProgramSequencePatternEvent event3593;
  ProgramSequencePatternEvent event3594;
  ProgramSequencePatternEvent event3595;
  ProgramSequencePatternEvent event3596;
  ProgramSequencePatternEvent event3597;
  ProgramSequencePatternEvent event3598;
  ProgramSequencePatternEvent event3599;
  ProgramSequencePatternEvent event3600;
  ProgramSequencePatternEvent event3601;
  ProgramSequencePatternEvent event3602;
  ProgramSequencePatternEvent event3603;
  ProgramSequencePatternEvent event3604;
  ProgramSequencePatternEvent event3605;
  ProgramSequencePatternEvent event3606;
  ProgramSequencePatternEvent event3607;
  ProgramSequencePatternEvent event3608;
  ProgramSequencePatternEvent event3609;
  ProgramSequencePatternEvent event3610;
  ProgramSequencePatternEvent event3611;
  ProgramSequencePatternEvent event3612;
  ProgramSequencePatternEvent event3613;
  ProgramSequencePatternEvent event3614;
  ProgramSequencePatternEvent event3615;
  ProgramSequencePatternEvent event3616;
  ProgramSequencePatternEvent event3617;
  ProgramSequencePatternEvent event3618;
  ProgramSequencePatternEvent event3619;
  ProgramSequencePatternEvent event3620;
  ProgramSequencePatternEvent event3621;
  ProgramSequencePatternEvent event3622;
  ProgramSequencePatternEvent event3623;
  ProgramSequencePatternEvent event3624;
  ProgramSequencePatternEvent event3625;
  ProgramSequencePatternEvent event3626;
  ProgramSequencePatternEvent event3627;
  ProgramSequencePatternEvent event3628;
  ProgramSequencePatternEvent event3629;
  ProgramSequencePatternEvent event3630;
  ProgramSequencePatternEvent event3631;
  ProgramSequencePatternEvent event3632;
  ProgramSequencePatternEvent event3633;
  ProgramSequencePatternEvent event3634;
  ProgramSequencePatternEvent event3635;
  ProgramSequencePatternEvent event3636;
  Program program6;
  ProgramVoice voice3637;
  ProgramVoice voice3638;
  ProgramVoice voice3639;
  ProgramVoice voice3640;
  ProgramVoice voice3641;
  ProgramVoice voice3642;
  ProgramVoice voice3643;
  ProgramVoiceTrack track3644;
  ProgramVoiceTrack track3645;
  ProgramVoiceTrack track3646;
  ProgramVoiceTrack track3647;
  ProgramVoiceTrack track3648;
  ProgramVoiceTrack track3649;
  ProgramSequence sequence3650;
  ProgramSequenceBinding sequenceBinding3651;
  ProgramSequencePattern pattern3652;
  ProgramSequencePattern pattern3653;
  ProgramSequencePattern pattern3654;
  ProgramSequencePattern pattern3655;
  ProgramSequencePattern pattern3656;
  ProgramSequencePattern pattern3657;
  ProgramSequencePattern pattern3658;
  ProgramSequencePatternEvent event3659;
  ProgramSequencePatternEvent event3660;
  ProgramSequencePatternEvent event3661;
  ProgramSequencePatternEvent event3662;
  ProgramSequencePatternEvent event3663;
  ProgramSequencePatternEvent event3664;
  ProgramSequencePatternEvent event3665;
  ProgramSequencePatternEvent event3666;
  ProgramSequencePatternEvent event3667;
  ProgramSequencePatternEvent event3668;
  ProgramSequencePatternEvent event3669;
  ProgramSequencePatternEvent event3670;
  ProgramSequencePatternEvent event3671;
  ProgramSequencePatternEvent event3672;
  ProgramSequencePatternEvent event3673;
  ProgramSequencePatternEvent event3674;
  ProgramSequencePatternEvent event3675;
  ProgramSequencePatternEvent event3676;
  ProgramSequencePatternEvent event3677;
  ProgramSequencePatternEvent event3678;
  ProgramSequencePatternEvent event3679;
  ProgramSequencePatternEvent event3680;
  ProgramSequencePatternEvent event3681;
  ProgramSequencePatternEvent event3682;
  ProgramSequencePatternEvent event3683;
  ProgramSequencePatternEvent event3684;
  ProgramSequencePatternEvent event3685;
  ProgramSequencePatternEvent event3686;
  ProgramSequencePatternEvent event3687;
  ProgramSequencePatternEvent event3688;
  ProgramSequencePatternEvent event3689;
  ProgramSequencePatternEvent event3690;
  ProgramSequencePatternEvent event3691;
  ProgramSequencePatternEvent event3692;
  ProgramSequencePatternEvent event3693;
  ProgramSequencePatternEvent event3694;
  ProgramSequencePatternEvent event3695;
  ProgramSequencePatternEvent event3696;

  /**
   Constructor

   @param db     database context
   @param access control
   */
  public ExperimentalMigration(DSLContext db, Access access) {
    this.db = db;
    this.access = access;
  }

  /**
   Insert Chain to database

   @param entity to insert
   @return the same chain (for chaining methods)
   */
  private <N extends Entity> N insert(N entity) throws CoreException {
    return DAORecord.insert(db, entity);
  }

  /**
   Go one

   @throws Exception
   */
  private void go1() throws Exception {
    // Insert User + UserRole records
    user1 = insert((User) User.create().setName("Charney Kaye").setEmail("charneykaye@gmail.com").setAvatarUrl("https://static.xj.io/charneykaye.jpg").setCreatedAt("2017-02-10T00:03:24Z"));
    insert(UserRole.create(user1, "user"));
    insert(UserRole.create(user1, "admin"));
    insert(UserRole.create(user1, "artist"));
    insert(UserRole.create(user1, "engineer"));
    user2 = insert((User) User.create().setName("Chris Luken").setEmail("christopher.luken@gmail.com").setAvatarUrl("https://lh6.googleusercontent.com/-LPlAziFhPyU/AAAAAAAAAAI/AAAAAAAAADA/P4VW3DIXFlw/photo.jpg?sz=50").setCreatedAt("2017-02-10T00:03:24Z"));
    insert(UserRole.create(user2, "user"));
    insert(UserRole.create(user2, "artist"));
    user3 = insert((User) User.create().setName("David Cole").setEmail("davecolemusic@gmail.com").setAvatarUrl("https://static.xj.io/davecole.jpg").setCreatedAt("2017-03-08T02:26:51Z"));
    insert(UserRole.create(user3, "Engineer"));
    insert(UserRole.create(user3, "User"));
    insert(UserRole.create(user3, "Artist"));
    user4 = insert((User) User.create().setName("Shannon Holloway").setEmail("shannon.holloway@gmail.com").setAvatarUrl("https://lh3.googleusercontent.com/-fvuNROyYKxk/AAAAAAAAAAI/AAAAAAAACo4/1d4e9rStIzY/photo.jpg?sz=50").setCreatedAt("2017-03-08T18:14:53Z"));
    insert(UserRole.create(user4, "user"));
    insert(UserRole.create(user4, "artist"));
    user5 = insert((User) User.create().setName("Lev Kaye").setEmail("lev@kaye.com").setAvatarUrl("https://lh3.googleusercontent.com/-Jq1k3laPQ08/AAAAAAAAAAI/AAAAAAAAAAA/l7dj-EXs8jQ/photo.jpg?sz=50").setCreatedAt("2017-03-09T23:47:12Z"));
    insert(UserRole.create(user5, "user"));
    insert(UserRole.create(user5, "artist"));
    user6 = insert((User) User.create().setName("Justin Knowlden (gus)").setEmail("gus@gusg.us").setAvatarUrl("https://lh4.googleusercontent.com/-U7mR8RgRhDE/AAAAAAAAAAI/AAAAAAAAB1k/VuF8nayQqdI/photo.jpg?sz=50").setCreatedAt("2017-04-14T20:41:41Z"));
    insert(UserRole.create(user6, "User"));
    insert(UserRole.create(user6, "Admin"));
    insert(UserRole.create(user6, "Engineer"));
    insert(UserRole.create(user6, "Artist"));
    user7 = insert((User) User.create().setName("dave farkas").setEmail("sakrafd@gmail.com").setAvatarUrl("https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50").setCreatedAt("2017-04-14T20:42:36Z"));
    insert(UserRole.create(user7, "Admin"));
    insert(UserRole.create(user7, "Engineer"));
    insert(UserRole.create(user7, "Artist"));
    insert(UserRole.create(user7, "User"));
    user8 = insert((User) User.create().setName("Aji Putra").setEmail("aji.perdana.putra@gmail.com").setAvatarUrl("https://lh5.googleusercontent.com/-yRjdJCgBHjQ/AAAAAAAAAAI/AAAAAAAABis/_Xue_78MM44/photo.jpg?sz=50").setCreatedAt("2017-04-21T17:33:25Z"));
    insert(UserRole.create(user8, "user"));
    user9 = insert((User) User.create().setName("live espn789").setEmail("scoreplace@gmail.com").setAvatarUrl("https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50").setCreatedAt("2017-04-21T19:13:22Z"));
    insert(UserRole.create(user9, "user"));
    user10 = insert((User) User.create().setName("Dmitry Solomadin").setEmail("dmitry.solomadin@gmail.com").setAvatarUrl("https://lh6.googleusercontent.com/-Ns78xq2VzKk/AAAAAAAAAAI/AAAAAAAAE44/ZOuBZnZqYeU/photo.jpg?sz=50").setCreatedAt("2017-05-03T21:09:33Z"));
    insert(UserRole.create(user10, "banned"));
    user11 = insert((User) User.create().setName("Michael Prolagaev").setEmail("prolagaev@gmail.com").setAvatarUrl("https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50").setCreatedAt("2017-05-04T16:13:06Z"));
    insert(UserRole.create(user11, "banned"));
    user12 = insert((User) User.create().setName("Charney Kaye").setEmail("nick.c.kaye@gmail.com").setAvatarUrl("https://lh5.googleusercontent.com/-_oXIqxZhTkk/AAAAAAAAAAI/AAAAAAAAUks/dg9oNRfPFco/photo.jpg?sz=50").setCreatedAt("2017-05-18T17:37:32Z"));
    insert(UserRole.create(user12, "user"));
    insert(UserRole.create(user12, "artist"));
    insert(UserRole.create(user12, "engineer"));
    user13 = insert((User) User.create().setName("Charney Kaye").setEmail("charney@outrightmental.com").setAvatarUrl("https://lh5.googleusercontent.com/-3yrpEvNKIvE/AAAAAAAAAAI/AAAAAAAAASc/Gls7ZJcVqCk/photo.jpg?sz=50").setCreatedAt("2017-06-19T20:39:46Z"));
    insert(UserRole.create(user13, "user"));
    insert(UserRole.create(user13, "artist"));
    user14 = insert((User) User.create().setName("Philip Z. Kimball").setEmail("pzkimball@pzklaw.com").setAvatarUrl("https://lh4.googleusercontent.com/-xnsM2SBKwaE/AAAAAAAAAAI/AAAAAAAAABs/uJouNj6fMgw/photo.jpg?sz=50").setCreatedAt("2017-06-26T13:56:57Z"));
    insert(UserRole.create(user14, "engineer"));
    insert(UserRole.create(user14, "user"));
    insert(UserRole.create(user14, "artist"));
    user15 = insert((User) User.create().setName("Janae' Leonard").setEmail("janaeleo55@gmail.com").setAvatarUrl("https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50").setCreatedAt("2017-06-28T09:30:40Z"));
    insert(UserRole.create(user15, "user"));
    user16 = insert((User) User.create().setName("yuan liu").setEmail("minamotoclan@gmail.com").setAvatarUrl("https://lh6.googleusercontent.com/-4orhpHPwHN4/AAAAAAAAAAI/AAAAAAAAFGc/HYueBarZIwA/photo.jpg?sz=50").setCreatedAt("2017-07-03T03:16:24Z"));
    insert(UserRole.create(user16, "user"));
    user17 = insert((User) User.create().setName("Nick Podgurski").setEmail("nickpodgurski@gmail.com").setAvatarUrl("https://lh5.googleusercontent.com/-Cly5aKHLBMc/AAAAAAAAAAI/AAAAAAAAAYQ/wu8BxP-Zwxk/photo.jpg?sz=50").setCreatedAt("2017-07-04T03:59:02Z"));
    insert(UserRole.create(user17, "user"));
    user18 = insert((User) User.create().setName("Brian Sweeny").setEmail("brian@vibesinternational.com").setAvatarUrl("https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50").setCreatedAt("2017-07-05T16:01:53Z"));
    insert(UserRole.create(user18, "user"));
    user19 = insert((User) User.create().setName("John Bennett").setEmail("johnalsobennett@gmail.com").setAvatarUrl("https://lh6.googleusercontent.com/-kFMmioNSrEM/AAAAAAAAAAI/AAAAAAAABfg/SfT2vo__XgI/photo.jpg?sz=50").setCreatedAt("2017-07-06T15:08:32Z"));
    insert(UserRole.create(user19, "user"));
    user20 = insert((User) User.create().setName("Aditi Hebbar").setEmail("adhebbar@gmail.com").setAvatarUrl("https://lh4.googleusercontent.com/-gUnZUky1WtE/AAAAAAAAAAI/AAAAAAAAEJ8/sFumIpFdaUA/photo.jpg?sz=50").setCreatedAt("2017-07-07T08:42:46Z"));
    insert(UserRole.create(user20, "user"));
    user21 = insert((User) User.create().setName("HANKYOL CHO").setEmail("hankyolcho@mail.adelphi.edu").setAvatarUrl("https://lh3.googleusercontent.com/-skrgmZw2fas/AAAAAAAAAAI/AAAAAAAAAAA/iwMwVr_CL2U/photo.jpg?sz=50").setCreatedAt("2017-07-10T14:10:03Z"));
    insert(UserRole.create(user21, "user"));
    user22 = insert((User) User.create().setName("Charles Frantz").setEmail("charlesfrantz@gmail.com").setAvatarUrl("https://lh4.googleusercontent.com/-WtgVMTchHkY/AAAAAAAAAAI/AAAAAAAAAMU/4hX0mxVuIBE/photo.jpg?sz=50").setCreatedAt("2017-07-13T14:28:39Z"));
    insert(UserRole.create(user22, "user"));
    insert(UserRole.create(user22, "artist"));
    insert(UserRole.create(user22, "engineer"));
    user23 = insert((User) User.create().setName("Alice Gamarnik").setEmail("ajgamarnik@gmail.com").setAvatarUrl("https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50").setCreatedAt("2017-07-14T16:25:46Z"));
    insert(UserRole.create(user23, "user"));
    insert(UserRole.create(user23, "artist"));
    user24 = insert((User) User.create().setName("liu xin").setEmail("xinliu2530@gmail.com").setAvatarUrl("https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50").setCreatedAt("2017-07-17T18:46:18Z"));
    insert(UserRole.create(user24, "user"));
    insert(UserRole.create(user24, "artist"));
    user25 = insert((User) User.create().setName("Outright Mental").setEmail("outrightmental@gmail.com").setAvatarUrl("https://lh5.googleusercontent.com/-2HcQgfYoQRU/AAAAAAAAAAI/AAAAAAAAANE/-ttDusZjeuk/photo.jpg?sz=50").setCreatedAt("2017-07-30T16:26:49Z"));
    insert(UserRole.create(user25, "artist"));
    insert(UserRole.create(user25, "engineer"));
    insert(UserRole.create(user25, "user"));
    user26 = insert((User) User.create().setName("Joey Lorjuste").setEmail("joeylorjuste@gmail.com").setAvatarUrl("https://lh4.googleusercontent.com/-WPQgkyb-M5A/AAAAAAAAAAI/AAAAAAAAH-Q/Lf9IG0JJl5c/photo.jpg?sz=50").setCreatedAt("2017-08-20T19:25:12Z"));
    insert(UserRole.create(user26, "user"));
    user27 = insert((User) User.create().setName("Mark Stewart").setEmail("mark.si.stewart@gmail.com").setAvatarUrl("https://static.xj.io/markstewart.jpg").setCreatedAt("2017-08-25T19:30:40Z"));
    insert(UserRole.create(user27, "user"));
    insert(UserRole.create(user27, "artist"));
    insert(UserRole.create(user27, "engineer"));
    user28 = insert((User) User.create().setName("Rosalind Kaye").setEmail("rckaye@kaye.com").setAvatarUrl("https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50").setCreatedAt("2017-10-16T00:11:49Z"));
    insert(UserRole.create(user28, "user"));
    user29 = insert((User) User.create().setName("Matthew DellaRatta").setEmail("mdellaratta8@gmail.com").setAvatarUrl("https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50").setCreatedAt("2017-10-17T00:00:36Z"));
    insert(UserRole.create(user29, "user"));
    user30 = insert((User) User.create().setName("Justice Whitaker").setEmail("justice512@gmail.com").setAvatarUrl("https://lh5.googleusercontent.com/-Y9sCwQKldqA/AAAAAAAAAAI/AAAAAAAAADE/3wU9xJLYRG0/photo.jpg?sz=50").setCreatedAt("2017-12-08T20:45:40Z"));
    insert(UserRole.create(user30, "user"));
    insert(UserRole.create(user30, "artist"));
    user31 = insert((User) User.create().setName("Ed Carney").setEmail("ed@steirmancpas.com").setAvatarUrl("https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg?sz=50").setCreatedAt("2017-12-13T15:13:49Z"));
    insert(UserRole.create(user31, "user"));
    user32 = insert((User) User.create().setName("Tamil Selvan").setEmail("prtamil@gmail.com").setAvatarUrl("https://lh6.googleusercontent.com/-iVWQ0iJwSvY/AAAAAAAAAAI/AAAAAAAAAJo/KlOXVs2XwFI/photo.jpg?sz=50").setCreatedAt("2018-02-04T08:50:11Z"));
    insert(UserRole.create(user32, "User"));
    user33 = insert((User) User.create().setName("Riyadh Abdullatif").setEmail("coldmo@gmail.com").setAvatarUrl("https://lh6.googleusercontent.com/-NQk0LpgjTc0/AAAAAAAAAAI/AAAAAAAAAGk/SCEchWKOh7g/photo.jpg?sz=50").setCreatedAt("2018-02-26T21:36:44Z"));
    insert(UserRole.create(user33, "User"));
    user34 = insert((User) User.create().setName("Ken Kaye").setEmail("ken@kaye.com").setAvatarUrl("https://lh3.googleusercontent.com/-r0rl7N0eE7g/AAAAAAAAAAI/AAAAAAAAAEc/IC1Dir_2XjE/photo.jpg?sz=50").setCreatedAt("2018-05-15T12:24:58Z"));
    insert(UserRole.create(user34, "User"));
    user35 = insert((User) User.create().setName("Eden Zhong").setEmail("hydrosulfate@gmail.com").setAvatarUrl("https://lh3.googleusercontent.com/-Ty-LN9tk8TQ/AAAAAAAAAAI/AAAAAAAADi4/J1bPsII4IFY/photo.jpg?sz=50").setCreatedAt("2018-06-04T20:57:29Z"));
    insert(UserRole.create(user35, "User"));
    user36 = insert((User) User.create().setName("Jacky Huang").setEmail("jackyxhu@usc.edu").setAvatarUrl("https://lh6.googleusercontent.com/-0MMr2iRpOHE/AAAAAAAAAAI/AAAAAAAAAH4/xXcj5T7YPbQ/photo.jpg?sz=50").setCreatedAt("2018-07-03T19:30:38Z"));
    insert(UserRole.create(user36, "User"));
    user37 = insert((User) User.create().setName("Simon Kalmus").setEmail("simon.kalmus@gmail.com").setAvatarUrl("https://lh3.googleusercontent.com/-ED1Ob9JwfVg/AAAAAAAAAAI/AAAAAAAACH0/jGu6rsiQiC4/photo.jpg?sz=50").setCreatedAt("2018-11-13T02:16:12Z"));
    insert(UserRole.create(user37, "User"));
    user38 = insert((User) User.create().setName("Charney Kaye").setEmail("charneykaye@gmail.com").setAvatarUrl("https://lh6.googleusercontent.com/-uVJxoVmL42M/AAAAAAAAAAI/AAAAAAAAp3I/O1sKk36Dfiw/s50/photo.jpg").setCreatedAt("2019-04-04T02:05:31Z"));
    insert(UserRole.create(user38, "User"));
    user39 = insert((User) User.create().setName("David Cole").setEmail("davecolemusic@gmail.com").setAvatarUrl("https://lh6.googleusercontent.com/-4KwLE88wOfw/AAAAAAAAAAI/AAAAAAAADa0/wfqGtqAR8wo/s50/photo.jpg").setCreatedAt("2019-04-04T02:20:04Z"));
    insert(UserRole.create(user39, "User"));
    user40 = insert((User) User.create().setName("Mark Stewart").setEmail("mark.si.stewart@gmail.com").setAvatarUrl("https://lh3.googleusercontent.com/-PtMRcK_-Bkg/AAAAAAAAAAI/AAAAAAAAASs/YlN0XjZSvdg/s50/photo.jpg").setCreatedAt("2019-04-06T05:16:16Z"));
    insert(UserRole.create(user40, "User"));
    user41 = insert((User) User.create().setName("Jay Whitaker").setEmail("jaywhitaker93@gmail.com").setAvatarUrl("https://lh6.googleusercontent.com/-TX6cFyyy4Lk/AAAAAAAAAAI/AAAAAAAAAAA/ACHi3rfOS7bcB1zZrLSP24erZqNVpql1Ow/s50-mo/photo.jpg").setCreatedAt("2019-06-11T02:23:39Z"));
    insert(UserRole.create(user41, "Artist"));
    insert(UserRole.create(user41, "User"));
    insert(UserRole.create(user41, "Engineer"));


    // Insert Account records
    account1 = insert(Account.create("Alpha"));
    account2 = insert(Account.create("Aircraftâ„¢"));
    account4 = insert(Account.create("Dave Cole Sandbox"));
    account5 = insert(Account.create("Mark Stewart Sandbox"));
    account6 = insert(Account.create("Jay Whitaker Sandbox"));


    // Insert AccountUser records
    insert(AccountUser.create(account1, user1));
    insert(AccountUser.create(account1, user2));
    insert(AccountUser.create(account2, user1));
    insert(AccountUser.create(account1, user3));
    insert(AccountUser.create(account1, user5));
    insert(AccountUser.create(account1, user6));
    insert(AccountUser.create(account1, user7));
    insert(AccountUser.create(account1, user12));
    insert(AccountUser.create(account1, user14));
    insert(AccountUser.create(account1, user22));
    insert(AccountUser.create(account1, user23));
    insert(AccountUser.create(account1, user24));
    insert(AccountUser.create(account1, user13));
    insert(AccountUser.create(account1, user25));
    insert(AccountUser.create(account1, user27));
    insert(AccountUser.create(account2, user3));
    insert(AccountUser.create(account2, user27));
    insert(AccountUser.create(account1, user28));
    insert(AccountUser.create(account1, user30));
    insert(AccountUser.create(account2, user6));
    insert(AccountUser.create(account2, user7));
    insert(AccountUser.create(account4, user3));
    insert(AccountUser.create(account5, user27));
    insert(AccountUser.create(account2, user14));
    insert(AccountUser.create(account2, user5));
    insert(AccountUser.create(account2, user41));
    insert(AccountUser.create(account6, user1));
    insert(AccountUser.create(account6, user41));


    // Insert Library records
    library1 = insert(Library.create(account1, "Pots and Pans #2"));
    library3 = insert(Library.create(account2, "Cool Ambienceâ„¢"));
    library4 = insert(Library.create(account4, "Test Library"));
    library5 = insert(Library.create(account5, "Test Library"));
  }


  /**
   Go 2

   @throws Exception
   */
  private void go2() throws Exception {
    // Insert Harmonic-type Instrument Earth Bass Harmony
    instrument9 = insert(Instrument.create(user1, library3, "Harmonic", "Published", "Earth Bass Harmony", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument9, "Earth"));
    // Instrument Audios
    audio0 = insert(InstrumentAudio.create(instrument9, "Omen Pad", "0732ee48-5a9b-4a1d-bafd-e8c2ef23231d-instrument-9-audio.wav", 0.000000, 4.450000, 88.000000, 65.333000, 0.600000));
    // Instrument Audio Events
    audioEvent1 = insert(InstrumentAudioEvent.create(audio0, 0.000000, 4.000000, "CRASH", "C2", 1.000000));
    // Instrument Audio Chords
    audioChord2 = insert(InstrumentAudioChord.create(audio0, 0.000000, "C"));
    audio3 = insert(InstrumentAudio.create(instrument9, "Bass Pad", "69dfbe99-bca5-4171-bbae-b69c4599531e-instrument-9-audio.wav", 0.000000, 4.073000, 88.000000, 49.606000, 0.600000));
    // Instrument Audio Events
    audioEvent4 = insert(InstrumentAudioEvent.create(audio3, 0.000000, 4.000000, "CRASH", "G1", 1.000000));
    // Instrument Audio Chords
    audioChord5 = insert(InstrumentAudioChord.create(audio3, 0.000000, "G"));


    // Insert Harmonic-type Instrument Fire String Hits
    instrument12 = insert(Instrument.create(user1, library3, "Harmonic", "Published", "Fire String Hits", 0.600000));
    // Instrument Memes
    // Instrument Audios
    audio6 = insert(InstrumentAudio.create(instrument12, "Koto", "eb8bca2c-994f-4e62-9bf6-1242acc79d21-instrument-12-audio.wav", 0.000000, 1.294000, 88.000000, 132.831000, 0.600000));
    // Instrument Audio Events
    audioEvent7 = insert(InstrumentAudioEvent.create(audio6, 0.000000, 1.000000, "CRASH", "C3", 1.000000));
    // Instrument Audio Chords
    audio8 = insert(InstrumentAudio.create(instrument12, "Shamisen", "e5eff131-8813-48bc-9bda-d378b3eeee9a-instrument-12-audio.wav", 0.005000, 1.353000, 88.000000, 264.072000, 0.600000));
    // Instrument Audio Events
    audioEvent9 = insert(InstrumentAudioEvent.create(audio8, 0.000000, 1.000000, "CRASH", "c4", 1.000000));
    // Instrument Audio Chords
    audio10 = insert(InstrumentAudio.create(instrument12, "Shami", "a166a69f-8944-4577-9a68-8b323dff7a68-instrument-12-audio.wav", 0.006000, 0.999000, 88.000000, 262.500000, 0.600000));
    // Instrument Audio Events
    audioEvent11 = insert(InstrumentAudioEvent.create(audio10, 0.000000, 1.000000, "CRASH", "c4", 1.000000));
    // Instrument Audio Chords


    // Insert Harmonic-type Instrument Water Sitar Harmony
    instrument8 = insert(Instrument.create(user1, library3, "Harmonic", "Published", "Water Sitar Harmony", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument8, "Water"));
    // Instrument Audios
    audio12 = insert(InstrumentAudio.create(instrument8, "Sitar", "9dc36d01-fd2e-49f7-a75b-545897962c9d-instrument-8-audio.wav", 0.000000, 2.424000, 88.000000, 262.500000, 0.600000));
    // Instrument Audio Events
    audioEvent13 = insert(InstrumentAudioEvent.create(audio12, 0.000000, 3.000000, "CRASH", "C4", 1.000000));
    // Instrument Audio Chords
    audioChord14 = insert(InstrumentAudioChord.create(audio12, 0.000000, "C"));


    // Insert Harmonic-type Instrument Water Whale Harmony
    instrument10 = insert(Instrument.create(user1, library3, "Harmonic", "Published", "Water Whale Harmony", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument10, "Water"));
    // Instrument Audios
    audio15 = insert(InstrumentAudio.create(instrument10, "Whale Pad", "c477ff4c-3212-4cfe-8712-6add5f697a98-instrument-10-audio.wav", 0.000000, 3.249000, 88.000000, 226.154000, 0.600000));
    // Instrument Audio Events
    audioEvent16 = insert(InstrumentAudioEvent.create(audio15, 0.000000, 4.000000, "CRASH", "A3", 1.000000));
    // Instrument Audio Chords
    audioChord17 = insert(InstrumentAudioChord.create(audio15, 0.000000, "A"));


    // Insert Harmonic-type Instrument Wind Flute Note
    instrument7 = insert(Instrument.create(user1, library3, "Harmonic", "Published", "Wind Flute Note", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument7, "Wind"));
    // Instrument Audios
    audio18 = insert(InstrumentAudio.create(instrument7, "Shakuhachi", "e02b5c6c-21a8-47b9-94fc-aaa5d1b2975f-instrument-7-audio.wav", 0.000000, 2.681000, 88.000000, 525.000000, 0.600000));
    // Instrument Audio Events
    audioEvent19 = insert(InstrumentAudioEvent.create(audio18, 0.000000, 4.000000, "CRASH", "C5", 1.000000));
    // Instrument Audio Chords
    audioChord20 = insert(InstrumentAudioChord.create(audio18, 0.000000, "C"));
    audio21 = insert(InstrumentAudio.create(instrument7, "Pan Flute", "de11db96-dfee-4fc3-8a02-3285d3bd2d80-instrument-7-audio.wav", 0.000000, 1.624000, 88.000000, 518.824000, 0.600000));
    // Instrument Audio Events
    audioEvent22 = insert(InstrumentAudioEvent.create(audio21, 0.000000, 2.000000, "CRASH", "C5", 1.000000));
    // Instrument Audio Chords
    audioChord23 = insert(InstrumentAudioChord.create(audio21, 0.000000, "C"));
    audio24 = insert(InstrumentAudio.create(instrument7, "Shamisen", "0e57fd93-11b6-49d8-b617-2d1b8e657180-instrument-12-audio.wav", 0.000000, 1.000000, 88.000000, 262.500000, 0.600000));
    // Instrument Audio Events
    audioEvent25 = insert(InstrumentAudioEvent.create(audio24, 0.000000, 1.000000, "CRASH", "C4", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument Acoustic
    instrument4 = insert(Instrument.create(user1, library1, "Percussive", "Published", "Acoustic", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument4, "Classic"));
    insert(InstrumentMeme.create(instrument4, "Hard"));
    insert(InstrumentMeme.create(instrument4, "Hot"));
    insert(InstrumentMeme.create(instrument4, "Deep"));
    insert(InstrumentMeme.create(instrument4, "Progressive"));
    insert(InstrumentMeme.create(instrument4, "Easy"));
    insert(InstrumentMeme.create(instrument4, "Tropical"));
    // Instrument Audios
    audio26 = insert(InstrumentAudio.create(instrument4, "Conga High", "511f5a68-1eca-4ca3-9713-956a219d734c-instrument-4-audio.wav", 0.002000, 0.425000, 120.000000, 187.660000, 0.600000));
    // Instrument Audio Events
    audioEvent27 = insert(InstrumentAudioEvent.create(audio26, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio28 = insert(InstrumentAudio.create(instrument4, "Vocal JB Get 2", "22efe6d1-3dea-45a5-906c-1e4bd4465606-instrument-4-audio.wav", 0.027000, 0.290000, 120.000000, 386.842000, 0.600000));
    // Instrument Audio Events
    audioEvent29 = insert(InstrumentAudioEvent.create(audio28, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio30 = insert(InstrumentAudio.create(instrument4, "Clap 2", "81f55d83-39fe-4832-99bf-4e4f3af69496-instrument-4-audio.wav", 0.000000, 0.684000, 120.000000, 188.462000, 0.600000));
    // Instrument Audio Events
    // Instrument Audio Chords
    audio31 = insert(InstrumentAudio.create(instrument4, "Snare Rim 5", "d404857a-6bf8-43c4-ad76-5259945d16fe-instrument-4-audio.wav", 0.000000, 0.463000, 120.000000, 181.481000, 0.600000));
    // Instrument Audio Events
    audioEvent32 = insert(InstrumentAudioEvent.create(audio31, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio33 = insert(InstrumentAudio.create(instrument4, "Tom", "d5bcc3a5-d98f-434f-8fcb-987f1913a684-instrument-4-audio.wav", 0.009000, 0.445000, 120.000000, 225.000000, 0.600000));
    // Instrument Audio Events
    audioEvent34 = insert(InstrumentAudioEvent.create(audio33, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio35 = insert(InstrumentAudio.create(instrument4, "Hihat Open 6", "9a57a402-98e9-4ceb-86c2-ea60607b56d1-instrument-4-audio.wav", 0.000000, 0.809000, 120.000000, 648.529000, 0.600000));
    // Instrument Audio Events
    audioEvent36 = insert(InstrumentAudioEvent.create(audio35, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio37 = insert(InstrumentAudio.create(instrument4, "Stick Side 5", "99f7dbea-c1fb-419e-ad44-c90804516aa3-instrument-4-audio.wav", 0.000000, 0.248000, 120.000000, 1837.500000, 0.600000));
    // Instrument Audio Events
    audioEvent38 = insert(InstrumentAudioEvent.create(audio37, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio39 = insert(InstrumentAudio.create(instrument4, "Hihat Open 5", "bf2c9ad8-ceb4-4c7e-98ae-a9c561680a1f-instrument-4-audio.wav", 0.003000, 1.115000, 120.000000, 648.529000, 0.600000));
    // Instrument Audio Events
    audioEvent40 = insert(InstrumentAudioEvent.create(audio39, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio41 = insert(InstrumentAudio.create(instrument4, "Vocal JB Baz", "76a3e02c-979c-4d64-9bab-3b1a91d3635d-instrument-4-audio.wav", 0.018000, 0.405000, 120.000000, 918.750000, 0.600000));
    // Instrument Audio Events
    audioEvent42 = insert(InstrumentAudioEvent.create(audio41, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio43 = insert(InstrumentAudio.create(instrument4, "Vocal Woo", "c7b78912-493a-4e19-a023-10a6b334e2b3-instrument-4-audio.wav", 0.010000, 0.522000, 120.000000, 464.211000, 0.600000));
    // Instrument Audio Events
    audioEvent44 = insert(InstrumentAudioEvent.create(audio43, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio45 = insert(InstrumentAudio.create(instrument4, "Hihat Closed 8", "cb1ffbff-c31d-4e06-9d84-649c1f257a24-instrument-4-audio.wav", 0.000000, 0.905000, 120.000000, 648.529000, 0.600000));
    // Instrument Audio Events
    audioEvent46 = insert(InstrumentAudioEvent.create(audio45, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio47 = insert(InstrumentAudio.create(instrument4, "Kick Long 2", "b12bf5ff-ebec-47e3-9259-6cd0c9f57724-instrument-4-audio.wav", 0.010000, 1.476000, 120.000000, 59.036000, 0.600000));
    // Instrument Audio Events
    audioEvent48 = insert(InstrumentAudioEvent.create(audio47, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio49 = insert(InstrumentAudio.create(instrument4, "Cymbal Crash 1", "378df92f-aec2-4a5c-9243-d08384971761-instrument-4-audio.wav", 0.018000, 1.878000, 120.000000, 1297.060000, 0.600000));
    // Instrument Audio Events
    audioEvent50 = insert(InstrumentAudioEvent.create(audio49, 0.000000, 4.000000, "CRASH", "x", 1.000000));
    // Instrument Audio Chords
    audio51 = insert(InstrumentAudio.create(instrument4, "Snare Rim 6", "5a840f38-7623-442b-b9a9-a0ff1927c7a0-instrument-4-audio.wav", 0.000000, 0.527000, 120.000000, 245.000000, 0.600000));
    // Instrument Audio Events
    audioEvent52 = insert(InstrumentAudioEvent.create(audio51, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio53 = insert(InstrumentAudio.create(instrument4, "Tom High", "4888db8b-1c81-4178-8af5-332ae7067ca8-instrument-4-audio.wav", 0.002000, 0.420000, 120.000000, 187.660000, 0.600000));
    // Instrument Audio Events
    audioEvent54 = insert(InstrumentAudioEvent.create(audio53, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio55 = insert(InstrumentAudio.create(instrument4, "Kick 3", "3a79549f-cf7b-4338-8756-f75b3fc5deaa-instrument-4-audio.wav", 0.005000, 0.742000, 120.000000, 52.128000, 0.600000));
    // Instrument Audio Events
    audioEvent56 = insert(InstrumentAudioEvent.create(audio55, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio57 = insert(InstrumentAudio.create(instrument4, "Tom 5", "bf45a337-c86a-4c44-9663-06093d3ca9ba-instrument-4-audio.wav", 0.000000, 0.590000, 120.000000, 90.928000, 0.600000));
    // Instrument Audio Events
    audioEvent58 = insert(InstrumentAudioEvent.create(audio57, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio59 = insert(InstrumentAudio.create(instrument4, "Vocal Eh", "a6049156-69e0-4128-a4b1-6a17ee4ca0bd-instrument-4-audio.wav", 0.018000, 0.449000, 120.000000, 668.182000, 0.600000));
    // Instrument Audio Events
    // Instrument Audio Chords
    audio60 = insert(InstrumentAudio.create(instrument4, "Stick Side 6", "0d65a838-e76f-407d-a06b-6485d67ba44c-instrument-4-audio.wav", 0.000000, 0.335000, 120.000000, 2321.050000, 0.600000));
    // Instrument Audio Events
    audioEvent61 = insert(InstrumentAudioEvent.create(audio60, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio62 = insert(InstrumentAudio.create(instrument4, "Snare Rim 7", "12e36076-5944-4101-a41b-b39136cf78a4-instrument-4-audio.wav", 0.000000, 0.461000, 120.000000, 254.913000, 0.600000));
    // Instrument Audio Events
    audioEvent63 = insert(InstrumentAudioEvent.create(audio62, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio64 = insert(InstrumentAudio.create(instrument4, "Tom High 5", "83294480-eef2-4171-8d69-8f16092557df-instrument-4-audio.wav", 0.003000, 0.444000, 120.000000, 126.000000, 0.600000));
    // Instrument Audio Events
    audioEvent65 = insert(InstrumentAudioEvent.create(audio64, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio66 = insert(InstrumentAudio.create(instrument4, "Cymbal Crash 2", "b921f58d-1ce0-4c1e-82d0-08479c25bfff-instrument-4-audio.wav", 0.010000, 3.241000, 120.000000, 469.149000, 0.600000));
    // Instrument Audio Events
    audioEvent67 = insert(InstrumentAudioEvent.create(audio66, 0.000000, 4.000000, "CRASH", "x", 1.000000));
    // Instrument Audio Chords
    audio68 = insert(InstrumentAudio.create(instrument4, "Vocal Ehh", "7806beda-4655-4323-adb0-d9a41d2fc939-instrument-4-audio.wav", 0.018000, 0.297000, 120.000000, 648.529000, 0.600000));
    // Instrument Audio Events
    audioEvent69 = insert(InstrumentAudioEvent.create(audio68, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio70 = insert(InstrumentAudio.create(instrument4, "Stick Side 7", "ea042c27-551b-44c7-998b-1df185d319cf-instrument-4-audio.wav", 0.003000, 0.159000, 120.000000, 436.634000, 0.600000));
    // Instrument Audio Events
    audioEvent71 = insert(InstrumentAudioEvent.create(audio70, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio72 = insert(InstrumentAudio.create(instrument4, "Conga", "2059cab7-8052-46cf-8fd1-2930cfe5ce59-instrument-4-audio.wav", 0.001000, 0.547000, 120.000000, 183.231000, 0.600000));
    // Instrument Audio Events
    audioEvent73 = insert(InstrumentAudioEvent.create(audio72, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio74 = insert(InstrumentAudio.create(instrument4, "Tom Low 5", "246190da-65fd-41a9-a943-2c8e3b763fa5-instrument-4-audio.wav", 0.000000, 0.730000, 120.000000, 84.483000, 0.600000));
    // Instrument Audio Events
    audioEvent75 = insert(InstrumentAudioEvent.create(audio74, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio76 = insert(InstrumentAudio.create(instrument4, "Snare Rim", "7b2d94b3-c218-498b-906e-11c313054cd1-instrument-4-audio.wav", 0.000000, 1.147000, 120.000000, 239.674000, 0.600000));
    // Instrument Audio Events
    audioEvent77 = insert(InstrumentAudioEvent.create(audio76, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio78 = insert(InstrumentAudio.create(instrument4, "Hihat Open 7", "4c3c5673-e8f1-4452-ad8c-5466cce0492d-instrument-4-audio.wav", 0.000000, 2.000000, 120.000000, 648.529000, 0.600000));
    // Instrument Audio Events
    audioEvent79 = insert(InstrumentAudioEvent.create(audio78, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio80 = insert(InstrumentAudio.create(instrument4, "Kick 7", "2fd75fb8-b968-46ba-8c43-ac6ad2db9a80-instrument-4-audio.wav", 0.008000, 0.537000, 120.000000, 43.534000, 0.600000));
    // Instrument Audio Events
    audioEvent81 = insert(InstrumentAudioEvent.create(audio80, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio82 = insert(InstrumentAudio.create(instrument4, "Vocal JB Get", "e5e8a85b-1c3c-46b5-8394-3b44b5c7e6e1-instrument-4-audio.wav", 0.027000, 0.311000, 120.000000, 386.842000, 0.600000));
    // Instrument Audio Events
    audioEvent83 = insert(InstrumentAudioEvent.create(audio82, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio84 = insert(InstrumentAudio.create(instrument4, "Vocal JB Me", "3fbbf18b-eb45-4375-8bd2-efd5e490c4cb-instrument-4-audio.wav", 14.000000, 0.336000, 120.000000, 367.500000, 0.600000));
    // Instrument Audio Events
    audioEvent85 = insert(InstrumentAudioEvent.create(audio84, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio86 = insert(InstrumentAudio.create(instrument4, "Snare 3", "d373a2f8-8c8f-4afa-b7e3-c21623d15f42-instrument-4-audio.wav", 0.008000, 0.404000, 120.000000, 2450.000000, 0.600000));
    // Instrument Audio Events
    audioEvent87 = insert(InstrumentAudioEvent.create(audio86, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio88 = insert(InstrumentAudio.create(instrument4, "Vocal JB Baz2", "94bd651e-ce98-4b09-95b8-6e36819e2721-instrument-4-audio.wav", 0.032000, 0.290000, 120.000000, 367.500000, 0.600000));
    // Instrument Audio Events
    audioEvent89 = insert(InstrumentAudioEvent.create(audio88, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio90 = insert(InstrumentAudio.create(instrument4, "Hihat Closed 7", "e15dc427-b556-4a72-bec8-6b59c6d8bbc8-instrument-4-audio.wav", 0.003000, 0.962000, 120.000000, 8820.000000, 0.600000));
    // Instrument Audio Events
    audioEvent91 = insert(InstrumentAudioEvent.create(audio90, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio92 = insert(InstrumentAudio.create(instrument4, "Hihat Closed 9", "0f28ef83-2213-4bbb-ae68-3eecc201ead3-instrument-4-audio.wav", 0.000000, 0.849000, 120.000000, 648.529000, 0.600000));
    // Instrument Audio Events
    audioEvent93 = insert(InstrumentAudioEvent.create(audio92, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio94 = insert(InstrumentAudio.create(instrument4, "Shim", "4a00b7f2-5634-40b0-801a-2f1f65a8cb54-instrument-4-audio.wav", 0.000000, 1.000000, 120.000000, 120.000000, 0.600000));
    // Instrument Audio Events
    // Instrument Audio Chords
    audio95 = insert(InstrumentAudio.create(instrument4, "Vocal JB Uhh", "3bc65d7a-00a0-42cc-9d15-292f9fbe98ee-instrument-4-audio.wav", 0.000000, 0.408000, 120.000000, 474.194000, 0.600000));
    // Instrument Audio Events
    audioEvent96 = insert(InstrumentAudioEvent.create(audio95, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio97 = insert(InstrumentAudio.create(instrument4, "Ugh", "8c37b691-4b93-47e8-9bbf-3821bdaf1bbc-instrument-4-audio.wav", 0.000000, 1.000000, 120.000000, 120.000000, 0.600000));
    // Instrument Audio Events
    // Instrument Audio Chords
    audio98 = insert(InstrumentAudio.create(instrument4, "Clap 1", "27b08205-9921-4d48-bc54-ba4110fe238f-instrument-4-audio.wav", 0.000000, 0.572000, 120.000000, 185.294000, 0.600000));
    // Instrument Audio Events
    audioEvent99 = insert(InstrumentAudioEvent.create(audio98, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio100 = insert(InstrumentAudio.create(instrument4, "Vocal JB Hit", "686906da-cc85-4abb-a902-121e98def35d-instrument-4-audio.wav", 0.050000, 0.313000, 120.000000, 512.791000, 0.600000));
    // Instrument Audio Events
    audioEvent101 = insert(InstrumentAudioEvent.create(audio100, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio102 = insert(InstrumentAudio.create(instrument4, "Snare 5", "cce1763b-fca3-49c5-9024-c665c1fea7f3-instrument-4-audio.wav", 0.008000, 0.407000, 120.000000, 180.738000, 0.600000));
    // Instrument Audio Events
    audioEvent103 = insert(InstrumentAudioEvent.create(audio102, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio104 = insert(InstrumentAudio.create(instrument4, "Snare 4", "511168e1-3291-4ec8-a6ac-652249206287-instrument-4-audio.wav", 0.008000, 0.439000, 120.000000, 204.167000, 0.600000));
    // Instrument Audio Events
    audioEvent105 = insert(InstrumentAudioEvent.create(audio104, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio106 = insert(InstrumentAudio.create(instrument4, "Cymbal Crash 3", "484d5dc0-4627-477d-8de7-f4c30cc4f538-instrument-4-audio.wav", 0.010000, 3.044000, 120.000000, 181.481000, 0.600000));
    // Instrument Audio Events
    audioEvent107 = insert(InstrumentAudioEvent.create(audio106, 0.000000, 4.000000, "CRASH", "x", 1.000000));
    // Instrument Audio Chords
    audio108 = insert(InstrumentAudio.create(instrument4, "Shammers", "b13399ea-a8be-44ec-9923-d096f6edccdc-instrument-4-audio.wav", 0.000000, 1.000000, 120.000000, 120.000000, 0.600000));
    // Instrument Audio Events
    // Instrument Audio Chords
    audio109 = insert(InstrumentAudio.create(instrument4, "Test", "579b3ad1-e23b-4a2c-bd07-a2c416e1edaf-instrument-4-audio.wav", 0.000000, 1.000000, 100.000000, 100.000000, 0.600000));
    // Instrument Audio Events
    // Instrument Audio Chords
    audio110 = insert(InstrumentAudio.create(instrument4, "Kick 3", "c076a674-1626-4b22-bc07-a639ca90b363-instrument-4-audio.wav", 0.010000, 0.677000, 120.000000, 56.178000, 0.600000));
    // Instrument Audio Events
    audioEvent111 = insert(InstrumentAudioEvent.create(audio110, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio112 = insert(InstrumentAudio.create(instrument4, "Vocal Hey", "5d808588-5930-4075-a034-4f96b0e2b06f-instrument-4-audio.wav", 0.046000, 0.453000, 120.000000, 760.345000, 0.600000));
    // Instrument Audio Events
    audioEvent113 = insert(InstrumentAudioEvent.create(audio112, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio114 = insert(InstrumentAudio.create(instrument4, "Cowbell", "392a388d-8e32-46f9-ad57-b3bd29929262-instrument-4-audio.wav", 0.002000, 0.298000, 120.000000, 525.000000, 0.600000));
    // Instrument Audio Events
    audioEvent115 = insert(InstrumentAudioEvent.create(audio114, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument Earth A (legacy)
    instrument33 = insert(Instrument.create(user1, library1, "Percussive", "Published", "Earth A (legacy)", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument33, "Earth"));
    // Instrument Audios
    audio116 = insert(InstrumentAudio.create(instrument33, "Snare Rim", "911ff421-989d-4c3d-8bc8-85eddc0d4d62-instrument-33-audio.wav", 0.000000, 1.147000, 120.000000, 239.674000, 0.600000));
    // Instrument Audio Events
    audioEvent117 = insert(InstrumentAudioEvent.create(audio116, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio118 = insert(InstrumentAudio.create(instrument33, "Conga High", "bf0ee9b7-de10-4826-b48a-5c3ae361258c-instrument-33-audio.wav", 0.002000, 0.425000, 120.000000, 187.660000, 0.600000));
    // Instrument Audio Events
    audioEvent119 = insert(InstrumentAudioEvent.create(audio118, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio120 = insert(InstrumentAudio.create(instrument33, "Tom Low 5", "3bfab5c2-974b-452d-b7ee-53347f0b12eb-instrument-33-audio.wav", 0.000000, 0.730000, 120.000000, 84.483000, 0.600000));
    // Instrument Audio Events
    audioEvent121 = insert(InstrumentAudioEvent.create(audio120, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio122 = insert(InstrumentAudio.create(instrument33, "Kick 7", "713f3c48-10ed-4f90-90a4-a28afc53f33e-instrument-33-audio.wav", 0.008000, 0.537000, 120.000000, 43.534000, 0.600000));
    // Instrument Audio Events
    audioEvent123 = insert(InstrumentAudioEvent.create(audio122, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio124 = insert(InstrumentAudio.create(instrument33, "Cymbal Crash 1", "505cba94-a3ff-4e64-8b25-dbe25176e410-instrument-33-audio.wav", 0.018000, 1.878000, 120.000000, 1297.060000, 0.600000));
    // Instrument Audio Events
    audioEvent125 = insert(InstrumentAudioEvent.create(audio124, 0.000000, 4.000000, "CRASH", "x", 1.000000));
    // Instrument Audio Chords
    audio126 = insert(InstrumentAudio.create(instrument33, "Hihat Closed 7", "cacdebae-527f-44ac-98f7-ba3bfeefa4fb-instrument-33-audio.wav", 0.003000, 0.962000, 120.000000, 8820.000000, 0.600000));
    // Instrument Audio Events
    audioEvent127 = insert(InstrumentAudioEvent.create(audio126, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio128 = insert(InstrumentAudio.create(instrument33, "Hihat Closed 9", "8059bae2-1e79-4623-839e-dae5dc29cb3c-instrument-33-audio.wav", 0.000000, 0.849000, 120.000000, 648.529000, 0.600000));
    // Instrument Audio Events
    audioEvent129 = insert(InstrumentAudioEvent.create(audio128, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio130 = insert(InstrumentAudio.create(instrument33, "Cowbell", "9602390a-51f8-44dc-a9a3-d40d9d26b509-instrument-33-audio.wav", 0.002000, 0.298000, 120.000000, 525.000000, 0.600000));
    // Instrument Audio Events
    audioEvent131 = insert(InstrumentAudioEvent.create(audio130, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio132 = insert(InstrumentAudio.create(instrument33, "Conga", "70fbac42-0c65-4acf-bc6d-216b656e1ab2-instrument-33-audio.wav", 0.001000, 0.547000, 120.000000, 183.231000, 0.600000));
    // Instrument Audio Events
    audioEvent133 = insert(InstrumentAudioEvent.create(audio132, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio134 = insert(InstrumentAudio.create(instrument33, "Hihat Open 5", "a3cda1c6-d620-4b5e-a819-0ab5fc49097a-instrument-33-audio.wav", 0.003000, 1.115000, 120.000000, 648.529000, 0.600000));
    // Instrument Audio Events
    audioEvent135 = insert(InstrumentAudioEvent.create(audio134, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio136 = insert(InstrumentAudio.create(instrument33, "Tom 5", "4840ae0e-5b15-4ad3-893f-78ba35950c8f-instrument-33-audio.wav", 0.000000, 0.590000, 120.000000, 90.928000, 0.600000));
    // Instrument Audio Events
    audioEvent137 = insert(InstrumentAudioEvent.create(audio136, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio138 = insert(InstrumentAudio.create(instrument33, "Snare Rim 7", "907fadd8-8e79-46a2-b6ae-6ba1068becbf-instrument-33-audio.wav", 0.000000, 0.461000, 120.000000, 254.913000, 0.600000));
    // Instrument Audio Events
    audioEvent139 = insert(InstrumentAudioEvent.create(audio138, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio140 = insert(InstrumentAudio.create(instrument33, "Hihat Open 7", "5ac13b1b-5314-4e10-b23f-3ee7e4ee832b-instrument-33-audio.wav", 0.000000, 2.000000, 120.000000, 648.529000, 0.600000));
    // Instrument Audio Events
    audioEvent141 = insert(InstrumentAudioEvent.create(audio140, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio142 = insert(InstrumentAudio.create(instrument33, "Stick Side 5", "a0f17131-c241-474d-8aa1-e955e3815e72-instrument-33-audio.wav", 0.000000, 0.248000, 120.000000, 1837.500000, 0.600000));
    // Instrument Audio Events
    audioEvent143 = insert(InstrumentAudioEvent.create(audio142, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio144 = insert(InstrumentAudio.create(instrument33, "Kick Long 2", "129a9d14-1cc3-4699-8428-457ee7cea45f-instrument-33-audio.wav", 0.010000, 1.476000, 120.000000, 59.036000, 0.600000));
    // Instrument Audio Events
    audioEvent145 = insert(InstrumentAudioEvent.create(audio144, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio146 = insert(InstrumentAudio.create(instrument33, "Hihat Closed 8", "03ebbe93-4900-4638-88d8-ddcc75a745ce-instrument-33-audio.wav", 0.000000, 0.905000, 120.000000, 648.529000, 0.600000));
    // Instrument Audio Events
    audioEvent147 = insert(InstrumentAudioEvent.create(audio146, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio148 = insert(InstrumentAudio.create(instrument33, "Stick Side 7", "34341f53-8995-4d09-9157-34f72feccaef-instrument-33-audio.wav", 0.003000, 0.159000, 120.000000, 436.634000, 0.600000));
    // Instrument Audio Events
    audioEvent149 = insert(InstrumentAudioEvent.create(audio148, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio150 = insert(InstrumentAudio.create(instrument33, "Tom High", "bcb153cd-b375-4da0-877d-add68dfbac71-instrument-33-audio.wav", 0.002000, 0.420000, 120.000000, 187.660000, 0.600000));
    // Instrument Audio Events
    audioEvent151 = insert(InstrumentAudioEvent.create(audio150, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio152 = insert(InstrumentAudio.create(instrument33, "Clap 2", "00646186-d34e-4924-aefd-6c7f8c35cd2c-instrument-33-audio.wav", 0.000000, 0.684000, 120.000000, 188.462000, 0.600000));
    // Instrument Audio Events
    // Instrument Audio Chords
    audio153 = insert(InstrumentAudio.create(instrument33, "Tom High 5", "acecd4e5-ec2e-40f9-a02e-a0903590c08b-instrument-33-audio.wav", 0.003000, 0.444000, 120.000000, 126.000000, 0.600000));
    // Instrument Audio Events
    audioEvent154 = insert(InstrumentAudioEvent.create(audio153, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio155 = insert(InstrumentAudio.create(instrument33, "Snare 5", "ed2f4c2d-0d10-4131-b948-53dd3c8fbed9-instrument-33-audio.wav", 0.008000, 0.407000, 120.000000, 180.738000, 0.600000));
    // Instrument Audio Events
    audioEvent156 = insert(InstrumentAudioEvent.create(audio155, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio157 = insert(InstrumentAudio.create(instrument33, "Kick 3", "b502bc7f-2569-48d4-a407-1f3be100c6c6-instrument-33-audio.wav", 0.010000, 0.677000, 120.000000, 56.178000, 0.600000));
    // Instrument Audio Events
    audioEvent158 = insert(InstrumentAudioEvent.create(audio157, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio159 = insert(InstrumentAudio.create(instrument33, "Snare Rim 6", "24f783b2-e67e-44bd-a65d-d772569a62a7-instrument-33-audio.wav", 0.000000, 0.527000, 120.000000, 245.000000, 0.600000));
    // Instrument Audio Events
    audioEvent160 = insert(InstrumentAudioEvent.create(audio159, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio161 = insert(InstrumentAudio.create(instrument33, "Snare 3", "f3635b83-f52d-4d6b-a447-73718408762a-instrument-33-audio.wav", 0.008000, 0.404000, 120.000000, 2450.000000, 0.600000));
    // Instrument Audio Events
    audioEvent162 = insert(InstrumentAudioEvent.create(audio161, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio163 = insert(InstrumentAudio.create(instrument33, "Cymbal Crash 2", "ec5c6494-21d6-44fc-9b47-72d9c321b3e3-instrument-33-audio.wav", 0.010000, 3.241000, 120.000000, 469.149000, 0.600000));
    // Instrument Audio Events
    audioEvent164 = insert(InstrumentAudioEvent.create(audio163, 0.000000, 4.000000, "CRASH", "x", 1.000000));
    // Instrument Audio Chords
    audio165 = insert(InstrumentAudio.create(instrument33, "Snare 4", "dcc530f8-ba21-4b98-aa3a-71168868b94a-instrument-33-audio.wav", 0.008000, 0.439000, 120.000000, 204.167000, 0.600000));
    // Instrument Audio Events
    audioEvent166 = insert(InstrumentAudioEvent.create(audio165, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio167 = insert(InstrumentAudio.create(instrument33, "Hihat Open 6", "3cfaedd6-d3f9-4a64-9353-ce4b91f4c59b-instrument-33-audio.wav", 0.000000, 0.809000, 120.000000, 648.529000, 0.600000));
    // Instrument Audio Events
    audioEvent168 = insert(InstrumentAudioEvent.create(audio167, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio169 = insert(InstrumentAudio.create(instrument33, "Snare Rim 5", "1a1fb20f-a03c-4640-a6d6-c650b1120998-instrument-33-audio.wav", 0.000000, 0.463000, 120.000000, 181.481000, 0.600000));
    // Instrument Audio Events
    audioEvent170 = insert(InstrumentAudioEvent.create(audio169, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio171 = insert(InstrumentAudio.create(instrument33, "Tom", "1493e34b-a70b-4c4b-b319-6fa154905d29-instrument-33-audio.wav", 0.009000, 0.445000, 120.000000, 225.000000, 0.600000));
    // Instrument Audio Events
    audioEvent172 = insert(InstrumentAudioEvent.create(audio171, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio173 = insert(InstrumentAudio.create(instrument33, "Clap 1", "27168cb8-ebc1-4e9a-aff1-b551cc7c9035-instrument-33-audio.wav", 0.000000, 0.572000, 120.000000, 185.294000, 0.600000));
    // Instrument Audio Events
    audioEvent174 = insert(InstrumentAudioEvent.create(audio173, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio175 = insert(InstrumentAudio.create(instrument33, "Stick Side 6", "610db17f-f525-4696-a6c9-122724e6bed5-instrument-33-audio.wav", 0.000000, 0.335000, 120.000000, 2321.050000, 0.600000));
    // Instrument Audio Events
    audioEvent176 = insert(InstrumentAudioEvent.create(audio175, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio177 = insert(InstrumentAudio.create(instrument33, "Kick 3", "94f456f1-832e-464b-8c0f-fd072e9c3d51-instrument-33-audio.wav", 0.005000, 0.742000, 120.000000, 52.128000, 0.600000));
    // Instrument Audio Events
    audioEvent178 = insert(InstrumentAudioEvent.create(audio177, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio179 = insert(InstrumentAudio.create(instrument33, "Cymbal Crash 3", "2119399f-7d57-4256-8195-0fd5c9318865-instrument-33-audio.wav", 0.010000, 3.044000, 120.000000, 181.481000, 0.600000));
    // Instrument Audio Events
    audioEvent180 = insert(InstrumentAudioEvent.create(audio179, 0.000000, 4.000000, "CRASH", "x", 1.000000));
    // Instrument Audio Chords
  }

  private void go3() throws Exception {
    // Insert Percussive-type Instrument Earth B (legacy)
    instrument34 = insert(Instrument.create(user1, library1, "Percussive", "Published", "Earth B (legacy)", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument34, "Earth"));
    // Instrument Audios
    audio181 = insert(InstrumentAudio.create(instrument34, "Tom 1", "042d2f0a-6e50-4e46-8842-315cee1e9bbf-instrument-34-audio.wav", 0.000000, 0.658000, 121.000000, 156.216000, 0.600000));
    // Instrument Audio Events
    audioEvent182 = insert(InstrumentAudioEvent.create(audio181, 0.000000, 1.000000, "TOM", "Eb3", 1.000000));
    // Instrument Audio Chords
    audio183 = insert(InstrumentAudio.create(instrument34, "Hihat Open 4", "e127c584-e507-43db-9a09-ede286f3855e-instrument-34-audio.wav", 0.048000, 0.456000, 121.000000, 4894.780000, 0.600000));
    // Instrument Audio Events
    audioEvent184 = insert(InstrumentAudioEvent.create(audio183, 0.000000, 0.500000, "HIHATOPEN", "Eb8", 1.000000));
    // Instrument Audio Chords
    audio185 = insert(InstrumentAudio.create(instrument34, "Kick B3", "956b5970-2552-4b2e-867e-53391e487b62-instrument-34-audio.wav", 0.000000, 0.719000, 121.000000, 76.085000, 0.600000));
    // Instrument Audio Events
    audioEvent186 = insert(InstrumentAudioEvent.create(audio185, 0.000000, 1.000000, "KICK", "Eb2", 1.000000));
    // Instrument Audio Chords
    audio187 = insert(InstrumentAudio.create(instrument34, "Hihat Closed 5", "ff67294e-2e40-430b-900e-97635b2fb28d-instrument-34-audio.wav", 0.000000, 0.397000, 121.000000, 3671.080000, 0.600000));
    // Instrument Audio Events
    audioEvent188 = insert(InstrumentAudioEvent.create(audio187, 0.000000, 0.500000, "HIHAT", "Bb7", 1.000000));
    // Instrument Audio Chords
    audio189 = insert(InstrumentAudio.create(instrument34, "Hihat Closed 2", "8ea17691-f68b-429f-9702-8b3a632fb143-instrument-34-audio.wav", 0.005000, 0.232000, 121.000000, 299.680000, 0.600000));
    // Instrument Audio Events
    audioEvent190 = insert(InstrumentAudioEvent.create(audio189, 0.000000, 0.500000, "HIHAT", "D4", 1.000000));
    // Instrument Audio Chords
    audio191 = insert(InstrumentAudio.create(instrument34, "Hihat Closed 4", "950b053a-d83f-4f26-860d-543694b2daf4-instrument-34-audio.wav", 0.005000, 0.447000, 121.000000, 4004.820000, 0.600000));
    // Instrument Audio Events
    audioEvent192 = insert(InstrumentAudioEvent.create(audio191, 0.000000, 0.500000, "HIHAT", "B7", 1.000000));
    // Instrument Audio Chords
    audio193 = insert(InstrumentAudio.create(instrument34, "Hihat Open 3", "c248a5f8-6d25-4db6-a8ab-f5aeb4df85b4-instrument-34-audio.wav", 0.048000, 0.453000, 121.000000, 4894.780000, 0.600000));
    // Instrument Audio Events
    audioEvent194 = insert(InstrumentAudioEvent.create(audio193, 0.000000, 0.500000, "HIHATOPEN", "Eb8", 1.000000));
    // Instrument Audio Chords
    audio195 = insert(InstrumentAudio.create(instrument34, "Hihat Open 2", "55633d22-d328-469d-98a0-ca771ad21a00-instrument-34-audio.wav", 0.050000, 0.467000, 121.000000, 4894.780000, 0.600000));
    // Instrument Audio Events
    audioEvent196 = insert(InstrumentAudioEvent.create(audio195, 0.000000, 0.500000, "HIHATOPEN", "Eb8", 1.000000));
    // Instrument Audio Chords
    audio197 = insert(InstrumentAudio.create(instrument34, "Tom 4", "7456aef7-0c4c-4cc5-a509-089389157349-instrument-34-audio.wav", 0.000000, 0.746000, 121.000000, 147.829000, 0.600000));
    // Instrument Audio Events
    audioEvent198 = insert(InstrumentAudioEvent.create(audio197, 0.000000, 1.000000, "TOM", "D3", 1.000000));
    // Instrument Audio Chords
    audio199 = insert(InstrumentAudio.create(instrument34, "Tom 2", "1393d95f-a8eb-40b2-ad39-bc09edfbdbdf-instrument-34-audio.wav", 0.000000, 0.207000, 121.000000, 157.896000, 0.600000));
    // Instrument Audio Events
    audioEvent200 = insert(InstrumentAudioEvent.create(audio199, 0.000000, 1.000000, "TOM", "Eb3", 1.000000));
    // Instrument Audio Chords
    audio201 = insert(InstrumentAudio.create(instrument34, "Snare 1", "44b5b9b4-7316-432f-b092-f98bcdfd07ac-instrument-34-audio.wav", 0.019000, 0.607000, 121.000000, 321.555000, 0.600000));
    // Instrument Audio Events
    audioEvent202 = insert(InstrumentAudioEvent.create(audio201, 0.000000, 1.000000, "SNARE", "E4", 1.000000));
    // Instrument Audio Chords
    audio203 = insert(InstrumentAudio.create(instrument34, "Crash 1", "82a7ed83-e780-4499-8847-5f38bde617c7-instrument-34-audio.wav", 0.000000, 4.032000, 121.000000, 4900.000000, 0.600000));
    // Instrument Audio Events
    audioEvent204 = insert(InstrumentAudioEvent.create(audio203, 0.000000, 4.000000, "CRASH", "Eb8", 1.000000));
    // Instrument Audio Chords
    audio205 = insert(InstrumentAudio.create(instrument34, "Kick B4", "35ac1df2-b427-4cb8-b8d0-dbbfbf092118-instrument-34-audio.wav", 0.000000, 0.844000, 121.000000, 3671.080000, 0.600000));
    // Instrument Audio Events
    audioEvent206 = insert(InstrumentAudioEvent.create(audio205, 0.000000, 1.000000, "KICK", "Bb4", 1.000000));
    // Instrument Audio Chords
    audio207 = insert(InstrumentAudio.create(instrument34, "Tom 5", "4cf09790-cf28-4dd2-9588-5df94368cec1-instrument-34-audio.wav", 0.000000, 0.780000, 121.000000, 148.828000, 0.600000));
    // Instrument Audio Events
    audioEvent208 = insert(InstrumentAudioEvent.create(audio207, 0.000000, 1.000000, "TOM", "D3", 1.000000));
    // Instrument Audio Chords
    audio209 = insert(InstrumentAudio.create(instrument34, "Snare 2", "84511e45-abbe-46c2-926a-b305dbb053a7-instrument-34-audio.wav", 0.015000, 0.489000, 121.000000, 917.771000, 0.600000));
    // Instrument Audio Events
    audioEvent210 = insert(InstrumentAudioEvent.create(audio209, 0.000000, 1.000000, "SNARE", "Bb5", 1.000000));
    // Instrument Audio Chords
    audio211 = insert(InstrumentAudio.create(instrument34, "Tom 3", "f6b2905a-6c8c-4a5c-9f78-5fbced18d5ad-instrument-34-audio.wav", 0.000000, 0.205000, 121.000000, 156.216000, 0.600000));
    // Instrument Audio Events
    audioEvent212 = insert(InstrumentAudioEvent.create(audio211, 0.000000, 1.000000, "TOM", "Eb3", 1.000000));
    // Instrument Audio Chords
    audio213 = insert(InstrumentAudio.create(instrument34, "Snare 5", "4f6bfbe3-a8f1-4d49-bd8d-b3a4a8d1e3a5-instrument-34-audio.wav", 0.012000, 0.570000, 121.000000, 270.264000, 0.600000));
    // Instrument Audio Events
    audioEvent214 = insert(InstrumentAudioEvent.create(audio213, 0.000000, 1.000000, "SNARE", "Db4", 1.000000));
    // Instrument Audio Chords
    audio215 = insert(InstrumentAudio.create(instrument34, "Snare 3", "6de10769-b73a-49c3-9e2d-a066caa2c4b6-instrument-34-audio.wav", 0.009000, 0.633000, 121.000000, 160.777000, 0.600000));
    // Instrument Audio Events
    audioEvent216 = insert(InstrumentAudioEvent.create(audio215, 0.000000, 1.000000, "SNARE", "E3", 1.000000));
    // Instrument Audio Chords
    audio217 = insert(InstrumentAudio.create(instrument34, "Snare 4", "8d18a3b1-bc07-46ae-a704-12141ffde112-instrument-34-audio.wav", 0.009000, 0.595000, 121.000000, 518.271000, 0.600000));
    // Instrument Audio Events
    audioEvent218 = insert(InstrumentAudioEvent.create(audio217, 0.000000, 1.000000, "SNARE", "C5", 1.000000));
    // Instrument Audio Chords
    audio219 = insert(InstrumentAudio.create(instrument34, "Hihat Open 1", "d4b1b624-3bc8-40c5-835e-b276b6ab4e93-instrument-34-audio.wav", 0.042000, 0.426000, 121.000000, 4894.780000, 0.600000));
    // Instrument Audio Events
    audioEvent220 = insert(InstrumentAudioEvent.create(audio219, 0.000000, 0.500000, "HIHATOPEN", "Eb8", 1.000000));
    // Instrument Audio Chords
    audio221 = insert(InstrumentAudio.create(instrument34, "Hihat Closed 1", "5a5f246d-3af0-4eaa-984b-df605e690e54-instrument-34-audio.wav", 0.014000, 0.357000, 121.000000, 1631.590000, 0.600000));
    // Instrument Audio Events
    audioEvent222 = insert(InstrumentAudioEvent.create(audio221, 0.000000, 0.500000, "HIHAT", "Ab6", 1.000000));
    // Instrument Audio Chords
    audio223 = insert(InstrumentAudio.create(instrument34, "Hihat Closed 3", "97f96d1f-7af4-4d3e-911e-654f12b0b06e-instrument-34-audio.wav", 0.010000, 0.248000, 121.000000, 3388.690000, 0.600000));
    // Instrument Audio Events
    audioEvent224 = insert(InstrumentAudioEvent.create(audio223, 0.000000, 0.500000, "HIHAT", "Ab7", 1.000000));
    // Instrument Audio Chords
    audio225 = insert(InstrumentAudio.create(instrument34, "Crash 2", "98212bf8-f414-45d2-b110-7feb549e4649-instrument-34-audio.wav", 0.000000, 4.024000, 121.000000, 4009.090000, 0.600000));
    // Instrument Audio Events
    audioEvent226 = insert(InstrumentAudioEvent.create(audio225, 0.000000, 4.000000, "CRASH", "B7", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument Earth Large
    instrument32 = insert(Instrument.create(user3, library3, "Percussive", "Published", "Earth Large", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument32, "Large"));
    insert(InstrumentMeme.create(instrument32, "Earth"));
    // Instrument Audios
    audio227 = insert(InstrumentAudio.create(instrument32, "Hi-Hat 8", "d6add194-6fa5-4cb7-ba33-8cc353e18486-instrument-32-audio.wav", 0.000000, 0.319000, 121.000000, 380.952000, 0.600000));
    // Instrument Audio Events
    audioEvent228 = insert(InstrumentAudioEvent.create(audio227, 0.000000, 1.000000, "HIHATOPEN", "G4", 1.000000));
    // Instrument Audio Chords
    audio229 = insert(InstrumentAudio.create(instrument32, "Hi-Hat 6", "f2e4b541-9a12-4ccd-b8ec-831dfa481075-instrument-32-audio.wav", 0.000000, 0.244000, 121.000000, 842.105000, 0.600000));
    // Instrument Audio Events
    audioEvent230 = insert(InstrumentAudioEvent.create(audio229, 0.000000, 1.000000, "HIHAT", "G#5", 1.000000));
    // Instrument Audio Chords
    audio231 = insert(InstrumentAudio.create(instrument32, "Kick 8", "1b7bb65b-9ce3-41db-bd80-9b7e2d8d07c1-instrument-32-audio.wav", 0.000000, 0.269000, 121.000000, 80.672000, 0.600000));
    // Instrument Audio Events
    audioEvent232 = insert(InstrumentAudioEvent.create(audio231, 0.000000, 1.000000, "KICK", "E2", 1.000000));
    // Instrument Audio Chords
    audio233 = insert(InstrumentAudio.create(instrument32, "Crash 3", "053b6a93-4cfc-4262-9e2e-d4e998849eed-instrument-32-audio.wav", 0.015000, 1.109000, 121.000000, 506.897000, 0.600000));
    // Instrument Audio Events
    audioEvent234 = insert(InstrumentAudioEvent.create(audio233, 0.000000, 4.000000, "CRASH", "B4", 1.000000));
    // Instrument Audio Chords
    audio235 = insert(InstrumentAudio.create(instrument32, "Snare 10", "9b9db8ec-f772-4369-8250-ed750493e569-instrument-32-audio.wav", 0.000000, 0.269000, 121.000000, 214.286000, 0.600000));
    // Instrument Audio Events
    audioEvent236 = insert(InstrumentAudioEvent.create(audio235, 0.000000, 1.000000, "SNARE", "A3", 1.000000));
    // Instrument Audio Chords
    audio237 = insert(InstrumentAudio.create(instrument32, "Snare 4", "362e452b-e168-4e06-aa04-bad9866b0cad-instrument-32-audio.wav", 0.000000, 0.232000, 121.000000, 279.070000, 0.600000));
    // Instrument Audio Events
    audioEvent238 = insert(InstrumentAudioEvent.create(audio237, 0.000000, 1.000000, "SNARE", "Db4", 1.000000));
    // Instrument Audio Chords
    audio239 = insert(InstrumentAudio.create(instrument32, "Crash 10", "5f59b257-1dba-44e1-9a15-86912f7250ce-instrument-32-audio.wav", 0.029000, 2.116000, 121.000000, 774.194000, 0.600000));
    // Instrument Audio Events
    audioEvent240 = insert(InstrumentAudioEvent.create(audio239, 0.000000, 4.000000, "CRASH", "G4", 1.000000));
    // Instrument Audio Chords
    audio241 = insert(InstrumentAudio.create(instrument32, "Kick 6", "ec419cb6-de9d-4ebc-8e8b-3ae98541819b-instrument-32-audio.wav", 0.006000, 0.360000, 121.000000, 78.947000, 0.600000));
    // Instrument Audio Events
    audioEvent242 = insert(InstrumentAudioEvent.create(audio241, 0.000000, 1.000000, "KICK", "Eb2", 1.000000));
    // Instrument Audio Chords
    audio243 = insert(InstrumentAudio.create(instrument32, "Snare 11", "0587a41b-ddf3-4802-a8b1-de671321a5c5-instrument-32-audio.wav", 0.000000, 0.237000, 121.000000, 4000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent244 = insert(InstrumentAudioEvent.create(audio243, 0.000000, 1.000000, "SNARE", "B7", 1.000000));
    // Instrument Audio Chords
    audio245 = insert(InstrumentAudio.create(instrument32, "Kick 9", "973a5ed8-3dd6-41db-a83a-1e3f52071a8e-instrument-32-audio.wav", 0.000000, 0.264000, 121.000000, 75.000000, 0.600000));
    // Instrument Audio Events
    audioEvent246 = insert(InstrumentAudioEvent.create(audio245, 0.000000, 1.000000, "KICK", "D2", 1.000000));
    // Instrument Audio Chords
    audio247 = insert(InstrumentAudio.create(instrument32, "Kick 10", "c6396c9f-1e57-46ff-acab-0f408ea87c2d-instrument-32-audio.wav", 0.000000, 0.280000, 121.000000, 66.946000, 0.600000));
    // Instrument Audio Events
    audioEvent248 = insert(InstrumentAudioEvent.create(audio247, 0.000000, 1.000000, "KICK", "C2", 1.000000));
    // Instrument Audio Chords
    audio249 = insert(InstrumentAudio.create(instrument32, "Snare 3", "c5091d09-3df0-4563-adc2-b36b4057a955-instrument-32-audio.wav", 0.000000, 0.269000, 121.000000, 4000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent250 = insert(InstrumentAudioEvent.create(audio249, 0.000000, 1.000000, "SNARE", "B7", 1.000000));
    // Instrument Audio Chords
    audio251 = insert(InstrumentAudio.create(instrument32, "Crash 7", "1f776bdc-517f-4a25-82cb-2abcac09cda0-instrument-32-audio.wav", 0.091000, 0.980000, 121.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent252 = insert(InstrumentAudioEvent.create(audio251, 0.000000, 2.000000, "CRASH", "F#6", 1.000000));
    // Instrument Audio Chords
    audio253 = insert(InstrumentAudio.create(instrument32, "Hi-Hat 4", "15d9f7f2-be82-4a30-9d82-e00b5628c78a-instrument-32-audio.wav", 0.000000, 0.287000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent254 = insert(InstrumentAudioEvent.create(audio253, 0.000000, 1.000000, "HIHATOPEN", "B8", 1.000000));
    // Instrument Audio Chords
    audio255 = insert(InstrumentAudio.create(instrument32, "Snare 5", "e3f9975f-1827-4853-a949-4a72fd01df41-instrument-32-audio.wav", 0.000000, 0.243000, 121.000000, 333.333000, 0.600000));
    // Instrument Audio Events
    audioEvent256 = insert(InstrumentAudioEvent.create(audio255, 0.000000, 1.000000, "SNARE", "E4", 1.000000));
    // Instrument Audio Chords
    audio257 = insert(InstrumentAudio.create(instrument32, "Kick 2 ", "63fb2984-fdf3-4100-86f1-5f70b77e0df8-instrument-32-audio.wav", 0.000000, 0.306000, 121.000000, 111.111000, 0.600000));
    // Instrument Audio Events
    audioEvent258 = insert(InstrumentAudioEvent.create(audio257, 0.000000, 1.000000, "KICK", "A2", 1.000000));
    // Instrument Audio Chords
    audio259 = insert(InstrumentAudio.create(instrument32, "Kick 1", "897ffb89-a52b-4223-92a7-529dd458c45a-instrument-32-audio.wav", 0.000000, 0.355000, 121.000000, 55.879000, 0.600000));
    // Instrument Audio Events
    audioEvent260 = insert(InstrumentAudioEvent.create(audio259, 0.000000, 1.000000, "KICK", "A1", 1.000000));
    // Instrument Audio Chords
    audio261 = insert(InstrumentAudio.create(instrument32, "Hi-Hat 7", "3f1fc10d-de1a-470a-8745-1c3580407016-instrument-32-audio.wav", 0.000000, 0.255000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent262 = insert(InstrumentAudioEvent.create(audio261, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords
    audio263 = insert(InstrumentAudio.create(instrument32, "Hi-Hat 2", "56d29d62-3a85-4ba8-8987-ffcda52e49e8-instrument-32-audio.wav", 0.000000, 0.297000, 121.000000, 9600.000000, 0.600000));
    // Instrument Audio Events
    audioEvent264 = insert(InstrumentAudioEvent.create(audio263, 0.000000, 1.000000, "HIHATOPEN", "D9", 1.000000));
    // Instrument Audio Chords
    audio265 = insert(InstrumentAudio.create(instrument32, "Kick 7", "77b26556-ce32-4500-9955-4508915e8b8e-instrument-32-audio.wav", 0.000000, 0.349000, 121.000000, 98.969000, 0.600000));
    // Instrument Audio Events
    audioEvent266 = insert(InstrumentAudioEvent.create(audio265, 0.000000, 1.000000, "KICK", "G2", 1.000000));
    // Instrument Audio Chords
    audio267 = insert(InstrumentAudio.create(instrument32, "Kick 5", "44c4ce78-ca9c-4dda-a931-7266c0a9295b-instrument-32-audio.wav", 0.000000, 0.269000, 121.000000, 100.840000, 0.600000));
    // Instrument Audio Events
    audioEvent268 = insert(InstrumentAudioEvent.create(audio267, 0.000000, 1.000000, "KICK", "G2", 1.000000));
    // Instrument Audio Chords
    audio269 = insert(InstrumentAudio.create(instrument32, "Kick 4", "41a86e00-13e1-43bd-b6a9-124dd8eab117-instrument-32-audio.wav", 0.002000, 0.285000, 121.000000, 113.475000, 0.600000));
    // Instrument Audio Events
    audioEvent270 = insert(InstrumentAudioEvent.create(audio269, 0.000000, 1.000000, "KICK", "Bb2", 1.000000));
    // Instrument Audio Chords
    audio271 = insert(InstrumentAudio.create(instrument32, "Snare 9", "9ca6169c-d5f6-4da7-ae33-901b909537d4-instrument-32-audio.wav", 0.000000, 0.275000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent272 = insert(InstrumentAudioEvent.create(audio271, 0.000000, 1.000000, "SNARE", "B8", 1.000000));
    // Instrument Audio Chords
    audio273 = insert(InstrumentAudio.create(instrument32, "Kenkeni 4", "5b5ae8cd-5987-49fa-9c48-bd461e0ef90a-instrument-28-audio.wav", 0.000000, 1.250000, 121.000000, 125.000000, 0.600000));
    // Instrument Audio Events
    audioEvent274 = insert(InstrumentAudioEvent.create(audio273, 0.000000, 1.000000, "TOM", "B2", 1.000000));
    // Instrument Audio Chords
    audio275 = insert(InstrumentAudio.create(instrument32, "Kenkeni 3", "554de69f-0434-42d0-989c-dc7a02179dec-instrument-28-audio.wav", 0.000000, 1.000000, 121.000000, 46.967000, 0.600000));
    // Instrument Audio Events
    audioEvent276 = insert(InstrumentAudioEvent.create(audio275, 0.000000, 1.000000, "TOM", "F#1", 1.000000));
    // Instrument Audio Chords
    audio277 = insert(InstrumentAudio.create(instrument32, "Snare 1", "c59874f8-5e5c-40e5-9761-75f32286c8b6-instrument-32-audio.wav", 0.000000, 0.344000, 121.000000, 159.468000, 0.600000));
    // Instrument Audio Events
    audioEvent278 = insert(InstrumentAudioEvent.create(audio277, 0.000000, 1.000000, "SNARE", "Eb3", 1.000000));
    // Instrument Audio Chords
    audio279 = insert(InstrumentAudio.create(instrument32, "Snare 6", "4070a2df-a5cd-434e-9902-7e607310de1d-instrument-32-audio.wav", 0.000000, 0.333000, 121.000000, 163.265000, 0.600000));
    // Instrument Audio Events
    audioEvent280 = insert(InstrumentAudioEvent.create(audio279, 0.000000, 1.000000, "SNARE", "E3", 1.000000));
    // Instrument Audio Chords
    audio281 = insert(InstrumentAudio.create(instrument32, "Snare 7", "c49cd26a-64e2-4a50-9133-55b18ce389b2-instrument-32-audio.wav", 0.000000, 0.232000, 121.000000, 169.014000, 0.600000));
    // Instrument Audio Events
    audioEvent282 = insert(InstrumentAudioEvent.create(audio281, 0.000000, 1.000000, "SNARE", "E3", 1.000000));
    // Instrument Audio Chords
    audio283 = insert(InstrumentAudio.create(instrument32, "Snare 8", "2e672449-b46e-4633-8758-72d18fb98b99-instrument-32-audio.wav", 0.000000, 0.333000, 121.000000, 206.009000, 0.600000));
    // Instrument Audio Events
    audioEvent284 = insert(InstrumentAudioEvent.create(audio283, 0.000000, 1.000000, "SNARE", "Ab3", 1.000000));
    // Instrument Audio Chords
    audio285 = insert(InstrumentAudio.create(instrument32, "Kick 3", "b0ceec8f-376c-479b-95b0-e61ed56de370-instrument-32-audio.wav", 0.003000, 0.487000, 121.000000, 85.868000, 0.600000));
    // Instrument Audio Events
    audioEvent286 = insert(InstrumentAudioEvent.create(audio285, 0.000000, 1.000000, "KICK", "F2", 1.000000));
    // Instrument Audio Chords
    audio287 = insert(InstrumentAudio.create(instrument32, "Kenkeni 7", "ac215ada-a632-456d-9894-305ca6e13fc0-instrument-28-audio.wav", 0.000300, 1.375000, 121.000000, 110.092000, 0.600000));
    // Instrument Audio Events
    audioEvent288 = insert(InstrumentAudioEvent.create(audio287, 0.000000, 1.000000, "TOM", "A2", 1.000000));
    // Instrument Audio Chords
    audio289 = insert(InstrumentAudio.create(instrument32, "Snare 2", "f0bd8b02-29d2-42d5-a122-da1c58408f77-instrument-32-audio.wav", 0.000000, 0.285000, 121.000000, 175.182000, 0.600000));
    // Instrument Audio Events
    audioEvent290 = insert(InstrumentAudioEvent.create(audio289, 0.000000, 1.000000, "SNARE", "F3", 1.000000));
    // Instrument Audio Chords
    audio291 = insert(InstrumentAudio.create(instrument32, "Djembe Rattle", "824ac18d-cf19-4ebe-9f4b-d2a16a20a526-instrument-28-audio.wav", 0.000300, 0.500000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent292 = insert(InstrumentAudioEvent.create(audio291, 0.000000, 1.000000, "TOM", "D6", 1.000000));
    // Instrument Audio Chords
    audio293 = insert(InstrumentAudio.create(instrument32, "Hi-Hat 1", "87dc20e3-67d8-4639-805f-3787d384a45a-instrument-32-audio.wav", 0.000000, 0.239000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent294 = insert(InstrumentAudioEvent.create(audio293, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument Earth Small
    instrument28 = insert(Instrument.create(user3, library3, "Percussive", "Published", "Earth Small", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument28, "Small"));
    insert(InstrumentMeme.create(instrument28, "Earth"));
    // Instrument Audios
    audio295 = insert(InstrumentAudio.create(instrument28, "Punchy Kick 4", "4066c8ba-28ad-464c-a87a-ebc6107b2de3-instrument-28-audio.wav", 0.000200, 0.375000, 121.000000, 95.618000, 0.600000));
    // Instrument Audio Events
    audioEvent296 = insert(InstrumentAudioEvent.create(audio295, 0.000000, 1.000000, "KICK", "G2", 1.000000));
    // Instrument Audio Chords
    audio297 = insert(InstrumentAudio.create(instrument28, "Long Snare", "f4144bde-9ccd-4851-b855-f00280689b2a-instrument-28-audio.wav", 0.000000, 0.375000, 121.000000, 142.433000, 0.600000));
    // Instrument Audio Events
    audioEvent298 = insert(InstrumentAudioEvent.create(audio297, 0.000000, 1.000000, "SNARE", "C#3", 1.000000));
    // Instrument Audio Chords
    audio299 = insert(InstrumentAudio.create(instrument28, "Shaker 2", "d48bc52b-95f4-4a5c-81f2-804ff13b84d3-instrument-28-audio.wav", 0.000000, 0.203000, 121.000000, 5333.330000, 0.600000));
    // Instrument Audio Events
    audioEvent300 = insert(InstrumentAudioEvent.create(audio299, 0.000000, 1.000000, "HIHAT", "E8", 1.000000));
    // Instrument Audio Chords
    audio301 = insert(InstrumentAudio.create(instrument28, "Weird Snap", "7793c912-032b-41e4-9fcf-c2d43df4389d-instrument-28-audio.wav", 0.000600, 0.250000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent302 = insert(InstrumentAudioEvent.create(audio301, 0.000000, 1.000000, "SNARE", "B8", 1.000000));
    // Instrument Audio Chords
    audio303 = insert(InstrumentAudio.create(instrument28, "Kenkeni 6", "8a4c6a10-841f-46d7-94e3-be8370624824-instrument-28-audio.wav", 0.000000, 0.500000, 121.000000, 4363.640000, 0.600000));
    // Instrument Audio Events
    audioEvent304 = insert(InstrumentAudioEvent.create(audio303, 0.000000, 1.000000, "TOM", "C#8", 1.000000));
    // Instrument Audio Chords
    audio305 = insert(InstrumentAudio.create(instrument28, "Open Kick", "24012246-e99a-4391-a1e9-800b5fcf3e7d-instrument-28-audio.wav", 0.000400, 0.625000, 121.000000, 85.409000, 0.600000));
    // Instrument Audio Events
    audioEvent306 = insert(InstrumentAudioEvent.create(audio305, 0.000000, 1.000000, "KICK", "F2", 1.000000));
    // Instrument Audio Chords
    audio307 = insert(InstrumentAudio.create(instrument28, "Kenkeni 2", "9edcee06-73e2-4fcd-80c1-d0b58ecf30c6-instrument-28-audio.wav", 0.000000, 1.500000, 121.000000, 85.258000, 0.600000));
    // Instrument Audio Events
    audioEvent308 = insert(InstrumentAudioEvent.create(audio307, 0.000000, 1.000000, "TOM", "F2", 1.000000));
    // Instrument Audio Chords
    audio309 = insert(InstrumentAudio.create(instrument28, "Punchy Kick", "5b1c7886-c39e-4904-8621-670e7b40f789-instrument-28-audio.wav", 0.012000, 0.375000, 121.000000, 67.321000, 0.600000));
    // Instrument Audio Events
    audioEvent310 = insert(InstrumentAudioEvent.create(audio309, 0.000000, 1.000000, "KICK", "C2", 1.000000));
    // Instrument Audio Chords
    audio311 = insert(InstrumentAudio.create(instrument28, "Djembe Slap ", "4685744e-ab0c-4cc0-904e-9c2698df6e96-instrument-28-audio.wav", 0.000400, 0.375000, 121.000000, 238.806000, 0.600000));
    // Instrument Audio Events
    audioEvent312 = insert(InstrumentAudioEvent.create(audio311, 0.000000, 1.000000, "TOM", "Bb3", 1.000000));
    // Instrument Audio Chords
    audio313 = insert(InstrumentAudio.create(instrument28, "Open Kick 3", "10290e3b-a443-4354-a82c-d153c21515ae-instrument-28-audio.wav", 0.000200, 0.375000, 121.000000, 124.031000, 0.600000));
    // Instrument Audio Events
    audioEvent314 = insert(InstrumentAudioEvent.create(audio313, 0.000000, 1.000000, "KICK", "B2", 1.000000));
    // Instrument Audio Chords
    audio315 = insert(InstrumentAudio.create(instrument28, "Punchy Kick 2", "a736fa1d-cece-4f2b-b2ab-966969c4b9af-instrument-28-audio.wav", 0.000100, 0.375000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent316 = insert(InstrumentAudioEvent.create(audio315, 0.000000, 1.000000, "KICK", "B", 1.000000));
    // Instrument Audio Chords
    audio317 = insert(InstrumentAudio.create(instrument28, "Punchy Kick 3", "ee1d1039-a388-4e5f-9504-77a40d59ab5f-instrument-28-audio.wav", 0.000200, 0.375000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent318 = insert(InstrumentAudioEvent.create(audio317, 0.000000, 1.000000, "KICK", "B8", 1.000000));
    // Instrument Audio Chords
    audio319 = insert(InstrumentAudio.create(instrument28, "Kenkeni 5", "ce6f3bb6-3d7d-4c2f-ab3a-70899d3f737e-instrument-28-audio.wav", 0.000000, 0.500000, 121.000000, 77.922000, 0.600000));
    // Instrument Audio Events
    audioEvent320 = insert(InstrumentAudioEvent.create(audio319, 0.000000, 1.000000, "TOM", "Eb2", 1.000000));
    // Instrument Audio Chords
    audio321 = insert(InstrumentAudio.create(instrument28, "Kenkeni", "e5c79d56-7dca-4dfe-8d3e-c6cd4215d798-instrument-28-audio.wav", 0.004700, 0.500000, 121.000000, 80.672000, 0.600000));
    // Instrument Audio Events
    audioEvent322 = insert(InstrumentAudioEvent.create(audio321, 0.000000, 1.000000, "TOM", "E2", 1.000000));
    // Instrument Audio Chords
    audio323 = insert(InstrumentAudio.create(instrument28, "Djembe Slap 2", "2d3b1bc7-919a-4419-abc4-25acdf57ee9d-instrument-28-audio.wav", 0.000900, 0.375000, 121.000000, 375.000000, 0.600000));
    // Instrument Audio Events
    audioEvent324 = insert(InstrumentAudioEvent.create(audio323, 0.000000, 1.000000, "TOM", "F#4", 1.000000));
    // Instrument Audio Chords
    audio325 = insert(InstrumentAudio.create(instrument28, "Fat Snare", "f92fa9e7-a7e9-43c0-93fa-4df241b476a9-instrument-28-audio.wav", 0.000000, 0.375000, 121.000000, 3692.310000, 0.600000));
    // Instrument Audio Events
    audioEvent326 = insert(InstrumentAudioEvent.create(audio325, 0.000000, 1.000000, "SNARE", "Bb7", 1.000000));
    // Instrument Audio Chords
    audio327 = insert(InstrumentAudio.create(instrument28, "Crash 1", "cd981b05-844f-4ede-b849-2acb4ad52537-instrument-28-audio.wav", 0.006000, 0.799000, 121.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent328 = insert(InstrumentAudioEvent.create(audio327, 0.000000, 4.000000, "CRASH", "G8", 1.000000));
    // Instrument Audio Chords
    audio329 = insert(InstrumentAudio.create(instrument28, "Snappy Rim Click", "cb5fb85c-4aa5-4359-824f-bff600563ea0-instrument-28-audio.wav", 0.000200, 0.375000, 121.000000, 1170.730000, 0.600000));
    // Instrument Audio Events
    audioEvent330 = insert(InstrumentAudioEvent.create(audio329, 0.000000, 1.000000, "SNARE", "D6", 1.000000));
    // Instrument Audio Chords
    audio331 = insert(InstrumentAudio.create(instrument28, "Djembe Palm", "0dcb66b5-e8e7-41b3-87df-7e3f5c0cd32d-instrument-28-audio.wav", 0.000000, 0.375000, 121.000000, 238.806000, 0.600000));
    // Instrument Audio Events
    audioEvent332 = insert(InstrumentAudioEvent.create(audio331, 0.000000, 1.000000, "TOM", "Bb3", 1.000000));
    // Instrument Audio Chords
    audio333 = insert(InstrumentAudio.create(instrument28, "Shaker", "74c870b3-df58-4aea-80ad-aa2d10b64bd4-instrument-28-audio.wav", 0.000000, 0.203000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent334 = insert(InstrumentAudioEvent.create(audio333, 0.000000, 1.000000, "HIHATOPEN", "F#8", 1.000000));
    // Instrument Audio Chords
    audio335 = insert(InstrumentAudio.create(instrument28, "Crash 14", "88c12e09-171c-4c4d-825d-0d9f6e486e89-instrument-28-audio.wav", 0.129000, 2.142000, 121.000000, 4000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent336 = insert(InstrumentAudioEvent.create(audio335, 0.000000, 4.000000, "CRASH", "B7", 1.000000));
    // Instrument Audio Chords
    audio337 = insert(InstrumentAudio.create(instrument28, "Shaker 3", "a20f3994-de53-4648-abb2-77370b098db5-instrument-28-audio.wav", 0.000000, 0.203000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent338 = insert(InstrumentAudioEvent.create(audio337, 0.000000, 1.000000, "HIHATOPEN", "F#9", 1.000000));
    // Instrument Audio Chords
    audio339 = insert(InstrumentAudio.create(instrument28, "Crispy Snare", "baebb6b9-a4b9-4686-9c78-1222135faff3-instrument-28-audio.wav", 0.002000, 0.250000, 121.000000, 187.500000, 0.600000));
    // Instrument Audio Events
    audioEvent340 = insert(InstrumentAudioEvent.create(audio339, 0.000000, 1.000000, "SNARE", "F#3", 1.000000));
    // Instrument Audio Chords
    audio341 = insert(InstrumentAudio.create(instrument28, "Open Kick 2", "6f884c6b-a519-4bff-aaac-ac9eb57fcade-instrument-28-audio.wav", 0.000000, 0.625000, 121.000000, 127.321000, 0.600000));
    // Instrument Audio Events
    audioEvent342 = insert(InstrumentAudioEvent.create(audio341, 0.000000, 1.000000, "KICK", "C3", 1.000000));
    // Instrument Audio Chords
    audio343 = insert(InstrumentAudio.create(instrument28, "Crash 6", "7135b241-ebbd-48fa-91b1-45d64489b2ce-instrument-28-audio.wav", 0.011000, 0.849000, 121.000000, 11025.000000, 0.600000));
    // Instrument Audio Events
    audioEvent344 = insert(InstrumentAudioEvent.create(audio343, 0.000000, 4.000000, "CRASH", "F9", 1.000000));
    // Instrument Audio Chords

  }


  private void go4() throws Exception {
    // Insert Percussive-type Instrument Electronic
    instrument3 = insert(Instrument.create(user1, library1, "Percussive", "Published", "Electronic", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument3, "Tech"));
    insert(InstrumentMeme.create(instrument3, "Electro"));
    insert(InstrumentMeme.create(instrument3, "Deep"));
    insert(InstrumentMeme.create(instrument3, "Cool"));
    insert(InstrumentMeme.create(instrument3, "Classic"));
    insert(InstrumentMeme.create(instrument3, "Hard"));
    insert(InstrumentMeme.create(instrument3, "Progressive"));
    insert(InstrumentMeme.create(instrument3, "Acid"));
    // Instrument Audios
    audio345 = insert(InstrumentAudio.create(instrument3, "Maracas 2", "f20dcce7-a936-446c-8692-c8caf37d8896-instrument-3-audio.wav", 0.009000, 0.430000, 120.000000, 11025.000000, 0.600000));
    // Instrument Audio Events
    audioEvent346 = insert(InstrumentAudioEvent.create(audio345, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio347 = insert(InstrumentAudio.create(instrument3, "Tom", "a6bf0d86-6b45-4cf1-b404-2242095c7876-instrument-3-audio.wav", 0.000000, 0.360000, 120.000000, 104.751000, 0.600000));
    // Instrument Audio Events
    audioEvent348 = insert(InstrumentAudioEvent.create(audio347, 0.000000, 0.350000, "TOM", "X", 0.700000));
    // Instrument Audio Chords
    audio349 = insert(InstrumentAudio.create(instrument3, "Tom 2", "3fcb76bf-6168-4aef-a160-facd1bb18071-instrument-3-audio.wav", 0.000000, 0.488000, 120.000000, 149.492000, 0.600000));
    // Instrument Audio Events
    audioEvent350 = insert(InstrumentAudioEvent.create(audio349, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio351 = insert(InstrumentAudio.create(instrument3, "Kick 2", "a731fc44-5ae0-4e9f-a728-edfe1895da4b-instrument-3-audio.wav", 0.000000, 0.340000, 120.000000, 69.122000, 0.600000));
    // Instrument Audio Events
    audioEvent352 = insert(InstrumentAudioEvent.create(audio351, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio353 = insert(InstrumentAudio.create(instrument3, "Clap 2", "9a3e9e07-b1dd-44a5-9399-3b6c11bd72b1-instrument-3-audio.wav", 0.002000, 0.356000, 120.000000, 1225.000000, 0.600000));
    // Instrument Audio Events
    audioEvent354 = insert(InstrumentAudioEvent.create(audio353, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio355 = insert(InstrumentAudio.create(instrument3, "Vocal Eow", "0e2d5fb2-9d40-4741-9da8-bc9943722d66-instrument-3-audio.wav", 0.045000, 0.486000, 120.000000, 383.478000, 0.600000));
    // Instrument Audio Events
    audioEvent356 = insert(InstrumentAudioEvent.create(audio355, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio357 = insert(InstrumentAudio.create(instrument3, "Kick Long", "ed1957b9-eea0-42f8-8493-b8874e1a6bf9-instrument-3-audio.wav", 0.000000, 0.865000, 120.000000, 57.050000, 0.600000));
    // Instrument Audio Events
    audioEvent358 = insert(InstrumentAudioEvent.create(audio357, 0.020000, 0.500000, "KICK", "X", 1.000000));
    // Instrument Audio Chords
    audio359 = insert(InstrumentAudio.create(instrument3, "Clap 3", "f24484dd-b879-42c5-9c2a-71857555c319-instrument-3-audio.wav", 0.000000, 0.734000, 120.000000, 980.000000, 0.600000));
    // Instrument Audio Events
    audioEvent360 = insert(InstrumentAudioEvent.create(audio359, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio361 = insert(InstrumentAudio.create(instrument3, "Cymbal Crash", "37a35a63-23e4-4ef6-a78e-db2577aa9a00-instrument-3-audio.wav", 0.000000, 2.229000, 120.000000, 109.701000, 0.600000));
    // Instrument Audio Events
    audioEvent362 = insert(InstrumentAudioEvent.create(audio361, 0.000000, 4.000000, "CRASH", "x", 1.000000));
    // Instrument Audio Chords
    audio363 = insert(InstrumentAudio.create(instrument3, "Vocal Hie", "0248ed87-19e8-449c-9211-4722d6ab8342-instrument-3-audio.wav", 0.100000, 0.477000, 120.000000, 364.463000, 0.600000));
    // Instrument Audio Events
    audioEvent364 = insert(InstrumentAudioEvent.create(audio363, 0.000000, 1.000000, "CRASH", "X", 1.000000));
    // Instrument Audio Chords
    audio365 = insert(InstrumentAudio.create(instrument3, "Hihat Closed", "0b7ea3d0-13ab-4c7c-ac66-1bec2e572c14-instrument-3-audio.wav", 0.000000, 0.053000, 120.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent366 = insert(InstrumentAudioEvent.create(audio365, 0.020000, 0.100000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio367 = insert(InstrumentAudio.create(instrument3, "Vocal Grunt Ooh 2", "8896e8d4-0c31-4dd8-93ff-6982a30febdb-instrument-3-audio.wav", 0.015000, 0.247000, 120.000000, 404.587000, 0.600000));
    // Instrument Audio Events
    audioEvent368 = insert(InstrumentAudioEvent.create(audio367, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio369 = insert(InstrumentAudio.create(instrument3, "Vocal Grunt Ooh", "ef489ad1-fb9d-4e77-9b5c-a7b3570c8c09-instrument-3-audio.wav", 0.011000, 0.213000, 120.000000, 1696.150000, 0.600000));
    // Instrument Audio Events
    audioEvent370 = insert(InstrumentAudioEvent.create(audio369, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio371 = insert(InstrumentAudio.create(instrument3, "Snare Rim", "58fd7eae-b55e-4567-9c27-ead64b83488a-instrument-3-audio.wav", 0.000000, 0.014000, 120.000000, 445.445000, 0.600000));
    // Instrument Audio Events
    audioEvent372 = insert(InstrumentAudioEvent.create(audio371, 0.000000, 0.014000, "SNARE", "x", 0.600000));
    // Instrument Audio Chords
    audio373 = insert(InstrumentAudio.create(instrument3, "Claves", "aea2483c-7707-4100-aa86-b680668cd1a0-instrument-3-audio.wav", 0.000000, 0.030000, 120.000000, 2594.000000, 0.600000));
    // Instrument Audio Events
    audioEvent374 = insert(InstrumentAudioEvent.create(audio373, 0.000000, 0.050000, "TOM", "X", 0.800000));
    // Instrument Audio Chords
    audio375 = insert(InstrumentAudio.create(instrument3, "Vocal Ahh", "d35678fa-f163-433d-8741-250a530b5532-instrument-3-audio.wav", 0.012000, 1.037000, 120.000000, 948.696000, 0.600000));
    // Instrument Audio Events
    audioEvent376 = insert(InstrumentAudioEvent.create(audio375, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio377 = insert(InstrumentAudio.create(instrument3, "Kick Long 2", "84b1974c-02b0-406f-b78e-21414282986e-instrument-3-audio.wav", 0.000000, 1.963000, 120.000000, 60.494000, 0.600000));
    // Instrument Audio Events
    audioEvent378 = insert(InstrumentAudioEvent.create(audio377, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio379 = insert(InstrumentAudio.create(instrument3, "Snare", "7ec44b7f-77fd-4a3a-a2df-f80f6cd7fcfe-instrument-3-audio.wav", 0.000000, 0.093000, 120.000000, 177.823000, 0.600000));
    // Instrument Audio Events
    audioEvent380 = insert(InstrumentAudioEvent.create(audio379, 0.000000, 0.091000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio381 = insert(InstrumentAudio.create(instrument3, "Cowbell", "aaa877a8-0c89-4781-93f8-69c722285b2a-instrument-3-audio.wav", 0.000000, 0.340000, 120.000000, 268.902000, 0.600000));
    // Instrument Audio Events
    audioEvent382 = insert(InstrumentAudioEvent.create(audio381, 0.000000, 0.300000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio383 = insert(InstrumentAudio.create(instrument3, "Conga", "f772f19f-b51b-414e-9dc8-8ceb23faa779-instrument-3-audio.wav", 0.000000, 0.260000, 120.000000, 213.000000, 0.600000));
    // Instrument Audio Events
    audioEvent384 = insert(InstrumentAudioEvent.create(audio383, 0.000000, 0.200000, "TOM", "X", 0.800000));
    // Instrument Audio Chords
    audio385 = insert(InstrumentAudio.create(instrument3, "Clap", "ce0662a2-3f7e-425b-8105-fb639d395235-instrument-3-audio.wav", 0.000000, 0.361000, 120.000000, 1102.500000, 0.600000));
    // Instrument Audio Events
    audioEvent386 = insert(InstrumentAudioEvent.create(audio385, 0.000000, 0.300000, "SNARE", "x", 0.800000));
    // Instrument Audio Chords
    audio387 = insert(InstrumentAudio.create(instrument3, "Vocal Hoo", "54d3503d-af44-4480-a0d0-8044fb403c5a-instrument-3-audio.wav", 0.079000, 0.450000, 120.000000, 205.116000, 0.600000));
    // Instrument Audio Events
    audioEvent388 = insert(InstrumentAudioEvent.create(audio387, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio389 = insert(InstrumentAudio.create(instrument3, "Conga High", "c0975d3a-4f26-44b2-a9d3-800320bfa3e1-instrument-3-audio.wav", 0.000000, 0.179000, 120.000000, 397.297000, 0.600000));
    // Instrument Audio Events
    audioEvent390 = insert(InstrumentAudioEvent.create(audio389, 0.000000, 0.200000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio391 = insert(InstrumentAudio.create(instrument3, "Maracas", "ffe4edd6-5b83-4ac9-8e69-156ddb06762f-instrument-3-audio.wav", 0.000000, 0.026000, 120.000000, 190.086000, 0.600000));
    // Instrument Audio Events
    audioEvent392 = insert(InstrumentAudioEvent.create(audio391, 0.010000, 0.015000, "HIHATOPEN", "X", 0.800000));
    // Instrument Audio Chords
    audio393 = insert(InstrumentAudio.create(instrument3, "Hihat Open", "020ad575-af86-4fe2-a869-957d50d59ac4-instrument-3-audio.wav", 0.000000, 0.598000, 120.000000, 7350.000000, 0.600000));
    // Instrument Audio Events
    audioEvent394 = insert(InstrumentAudioEvent.create(audio393, 0.000000, 0.590000, "HIHATOPEN", "x", 0.500000));
    // Instrument Audio Chords
    audio395 = insert(InstrumentAudio.create(instrument3, "Cymbal Crash 2", "bb3e2a48-8f59-4ad0-a05f-30aca579524f-instrument-3-audio.wav", 0.000000, 2.000000, 120.000000, 816.667000, 0.600000));
    // Instrument Audio Events
    audioEvent396 = insert(InstrumentAudioEvent.create(audio395, 0.000000, 4.000000, "CRASH", "x", 1.000000));
    // Instrument Audio Chords
    audio397 = insert(InstrumentAudio.create(instrument3, "Vocal Haa", "79b9c4f4-037a-4f6f-bc51-7a7a2dff5528-instrument-3-audio.wav", 0.053000, 0.360000, 120.000000, 864.706000, 0.600000));
    // Instrument Audio Events
    audioEvent398 = insert(InstrumentAudioEvent.create(audio397, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio399 = insert(InstrumentAudio.create(instrument3, "Kick", "80454e35-5693-4b42-aa6a-218383a9f584-instrument-3-audio.wav", 0.000000, 0.702000, 120.000000, 57.495000, 0.600000));
    // Instrument Audio Events
    audioEvent400 = insert(InstrumentAudioEvent.create(audio399, 0.030000, 0.500000, "KICK", "X", 1.000000));
    // Instrument Audio Chords
    audio401 = insert(InstrumentAudio.create(instrument3, "Tom High 2", "618bc8e5-f51f-4635-895c-5bd6522f8d8c-instrument-3-audio.wav", 0.002000, 0.411000, 120.000000, 201.370000, 0.600000));
    // Instrument Audio Events
    audioEvent402 = insert(InstrumentAudioEvent.create(audio401, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio403 = insert(InstrumentAudio.create(instrument3, "Tom High", "aea1351b-bb96-4487-8feb-ae8ad3e499ad-instrument-3-audio.wav", 0.000000, 0.200000, 120.000000, 190.909000, 0.600000));
    // Instrument Audio Events
    audioEvent404 = insert(InstrumentAudioEvent.create(audio403, 0.000000, 0.200000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio405 = insert(InstrumentAudio.create(instrument3, "Vocal How", "f70ead8e-f770-4782-83ce-854a1cb3c640-instrument-3-audio.wav", 0.074000, 0.454000, 120.000000, 284.516000, 0.600000));
    // Instrument Audio Events
    // Instrument Audio Chords
    audio406 = insert(InstrumentAudio.create(instrument3, "Tom Low 2", "014c8939-c9e7-4911-9620-9c4075a3b4a2-instrument-3-audio.wav", 0.000000, 0.701000, 120.000000, 111.646000, 0.600000));
    // Instrument Audio Events
    audioEvent407 = insert(InstrumentAudioEvent.create(audio406, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument Fire A  (legacy)
    instrument35 = insert(Instrument.create(user1, library1, "Percussive", "Published", "Fire A  (legacy)", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument35, "Fire"));
    // Instrument Audios
    audio408 = insert(InstrumentAudio.create(instrument35, "Hihat Open", "7067e7ee-f192-459a-8733-49e550467c67-instrument-35-audio.wav", 0.000000, 0.598000, 120.000000, 7350.000000, 0.600000));
    // Instrument Audio Events
    audioEvent409 = insert(InstrumentAudioEvent.create(audio408, 0.000000, 0.590000, "HIHATOPEN", "x", 0.500000));
    // Instrument Audio Chords
    audio410 = insert(InstrumentAudio.create(instrument35, "Tom", "794713a0-e00d-4918-a729-7d343e09c719-instrument-35-audio.wav", 0.000000, 0.360000, 120.000000, 104.751000, 0.600000));
    // Instrument Audio Events
    audioEvent411 = insert(InstrumentAudioEvent.create(audio410, 0.000000, 0.350000, "TOM", "X", 0.700000));
    // Instrument Audio Chords
    audio412 = insert(InstrumentAudio.create(instrument35, "Cowbell", "fd71cbf9-1677-49d6-b4d5-60ea65d78d09-instrument-35-audio.wav", 0.000000, 0.340000, 120.000000, 268.902000, 0.600000));
    // Instrument Audio Events
    audioEvent413 = insert(InstrumentAudioEvent.create(audio412, 0.000000, 0.300000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio414 = insert(InstrumentAudio.create(instrument35, "Conga High", "fe30bbf3-a789-4c71-97b4-40f2746139be-instrument-35-audio.wav", 0.000000, 0.179000, 120.000000, 397.297000, 0.600000));
    // Instrument Audio Events
    audioEvent415 = insert(InstrumentAudioEvent.create(audio414, 0.000000, 0.200000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio416 = insert(InstrumentAudio.create(instrument35, "Clap 3", "7ff0e518-ab41-469f-89d2-a28b8bdba0e9-instrument-35-audio.wav", 0.000000, 0.734000, 120.000000, 980.000000, 0.600000));
    // Instrument Audio Events
    audioEvent417 = insert(InstrumentAudioEvent.create(audio416, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio418 = insert(InstrumentAudio.create(instrument35, "Snare Rim", "e149c7fb-76bc-4ef5-92eb-38498f87e768-instrument-35-audio.wav", 0.000000, 0.014000, 120.000000, 445.445000, 0.600000));
    // Instrument Audio Events
    audioEvent419 = insert(InstrumentAudioEvent.create(audio418, 0.000000, 0.014000, "SNARE", "x", 0.600000));
    // Instrument Audio Chords
    audio420 = insert(InstrumentAudio.create(instrument35, "Claves", "5f76cf95-4e48-47bb-b161-2fe38871c72e-instrument-35-audio.wav", 0.000000, 0.030000, 120.000000, 2594.000000, 0.600000));
    // Instrument Audio Events
    audioEvent421 = insert(InstrumentAudioEvent.create(audio420, 0.000000, 0.050000, "TOM", "X", 0.800000));
    // Instrument Audio Chords
    audio422 = insert(InstrumentAudio.create(instrument35, "Kick 2", "89f40f31-a538-4afa-871d-861f555f7dbe-instrument-35-audio.wav", 0.000000, 0.340000, 120.000000, 69.122000, 0.600000));
    // Instrument Audio Events
    audioEvent423 = insert(InstrumentAudioEvent.create(audio422, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio424 = insert(InstrumentAudio.create(instrument35, "Kick Long 2", "a47b2510-6e4a-4933-a51a-8a14b2218c40-instrument-35-audio.wav", 0.000000, 1.963000, 120.000000, 60.494000, 0.600000));
    // Instrument Audio Events
    audioEvent425 = insert(InstrumentAudioEvent.create(audio424, 0.000000, 1.000000, "KICK", "x", 1.000000));
    // Instrument Audio Chords
    audio426 = insert(InstrumentAudio.create(instrument35, "Clap", "094e7d52-866d-4514-8bb7-72c124c4c3ff-instrument-35-audio.wav", 0.000000, 0.361000, 120.000000, 1102.500000, 0.600000));
    // Instrument Audio Events
    audioEvent427 = insert(InstrumentAudioEvent.create(audio426, 0.000000, 0.300000, "SNARE", "x", 0.800000));
    // Instrument Audio Chords
    audio428 = insert(InstrumentAudio.create(instrument35, "Clap 2", "281e202c-cb93-49ba-81cc-06c20e218f1e-instrument-35-audio.wav", 0.002000, 0.356000, 120.000000, 1225.000000, 0.600000));
    // Instrument Audio Events
    audioEvent429 = insert(InstrumentAudioEvent.create(audio428, 0.000000, 1.000000, "SNARE", "x", 1.000000));
    // Instrument Audio Chords
    audio430 = insert(InstrumentAudio.create(instrument35, "Tom 2", "2b6e6271-6093-4942-b892-5a92fbe92fd8-instrument-35-audio.wav", 0.000000, 0.488000, 120.000000, 149.492000, 0.600000));
    // Instrument Audio Events
    audioEvent431 = insert(InstrumentAudioEvent.create(audio430, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio432 = insert(InstrumentAudio.create(instrument35, "Kick Long", "04cc3d33-9fe5-4664-a4c9-4a9e7b480bd2-instrument-35-audio.wav", 0.000000, 0.865000, 120.000000, 57.050000, 0.600000));
    // Instrument Audio Events
    audioEvent433 = insert(InstrumentAudioEvent.create(audio432, 0.020000, 0.500000, "KICK", "X", 1.000000));
    // Instrument Audio Chords
    audio434 = insert(InstrumentAudio.create(instrument35, "Tom High", "cbff0b34-7248-4dd7-88d6-5b17eb8a15a2-instrument-35-audio.wav", 0.000000, 0.200000, 120.000000, 190.909000, 0.600000));
    // Instrument Audio Events
    audioEvent435 = insert(InstrumentAudioEvent.create(audio434, 0.000000, 0.200000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio436 = insert(InstrumentAudio.create(instrument35, "Tom High 2", "66b4998e-7ad9-45ff-abdc-c803267f36e6-instrument-35-audio.wav", 0.002000, 0.411000, 120.000000, 201.370000, 0.600000));
    // Instrument Audio Events
    audioEvent437 = insert(InstrumentAudioEvent.create(audio436, 0.000000, 1.000000, "TOM", "x", 1.000000));
    // Instrument Audio Chords
    audio438 = insert(InstrumentAudio.create(instrument35, "Kick", "324b83d8-2993-4a49-83a1-78f3425c3ac4-instrument-35-audio.wav", 0.000000, 0.702000, 120.000000, 57.495000, 0.600000));
    // Instrument Audio Events
    audioEvent439 = insert(InstrumentAudioEvent.create(audio438, 0.030000, 0.500000, "KICK", "X", 1.000000));
    // Instrument Audio Chords
    audio440 = insert(InstrumentAudio.create(instrument35, "Hihat Closed", "5a62482a-3d44-493e-95e5-e5b4e5747bd9-instrument-35-audio.wav", 0.000000, 0.053000, 120.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent441 = insert(InstrumentAudioEvent.create(audio440, 0.020000, 0.100000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio442 = insert(InstrumentAudio.create(instrument35, "Cymbal Crash 2", "d2ba2873-c970-45de-afc7-f2d405784376-instrument-35-audio.wav", 0.000000, 2.000000, 120.000000, 816.667000, 0.600000));
    // Instrument Audio Events
    audioEvent443 = insert(InstrumentAudioEvent.create(audio442, 0.000000, 4.000000, "CRASH", "x", 1.000000));
    // Instrument Audio Chords
    audio444 = insert(InstrumentAudio.create(instrument35, "Maracas 2", "71282f6f-efdb-4af2-a2f3-c0dc03115853-instrument-35-audio.wav", 0.009000, 0.430000, 120.000000, 11025.000000, 0.600000));
    // Instrument Audio Events
    audioEvent445 = insert(InstrumentAudioEvent.create(audio444, 0.000000, 1.000000, "HIHATOPEN", "x", 1.000000));
    // Instrument Audio Chords
    audio446 = insert(InstrumentAudio.create(instrument35, "Conga", "5c180a6b-9586-4f1d-8df2-631cb63774a8-instrument-35-audio.wav", 0.000000, 0.260000, 120.000000, 213.000000, 0.600000));
    // Instrument Audio Events
    audioEvent447 = insert(InstrumentAudioEvent.create(audio446, 0.000000, 0.200000, "TOM", "X", 0.800000));
    // Instrument Audio Chords
    audio448 = insert(InstrumentAudio.create(instrument35, "Maracas", "411aff24-ac6b-4617-a483-303b050ff502-instrument-35-audio.wav", 0.000000, 0.026000, 120.000000, 190.086000, 0.600000));
    // Instrument Audio Events
    audioEvent449 = insert(InstrumentAudioEvent.create(audio448, 0.010000, 0.015000, "HIHATOPEN", "X", 0.800000));
    // Instrument Audio Chords
    audio450 = insert(InstrumentAudio.create(instrument35, "Snare", "03d347ba-e65a-481d-954f-eb8f64460e41-instrument-35-audio.wav", 0.000000, 0.093000, 120.000000, 177.823000, 0.600000));
    // Instrument Audio Events
    audioEvent451 = insert(InstrumentAudioEvent.create(audio450, 0.000000, 0.091000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio452 = insert(InstrumentAudio.create(instrument35, "Cymbal Crash", "03111025-2607-45f6-b48c-a6abb249c4a9-instrument-35-audio.wav", 0.000000, 2.229000, 120.000000, 109.701000, 0.600000));
    // Instrument Audio Events
    audioEvent453 = insert(InstrumentAudioEvent.create(audio452, 0.000000, 4.000000, "CRASH", "x", 1.000000));
    // Instrument Audio Chords

  }

  private void go5() throws CoreException {
    // Insert Percussive-type Instrument Fire Large
    instrument41 = insert(Instrument.create(user3, library3, "Percussive", "Published", "Fire Large", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument41, "Fire"));
    insert(InstrumentMeme.create(instrument41, "Large"));
    // Instrument Audios
    audio454 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 3", "6c85494e-fc05-46b0-8e98-97c3d756ebc0-instrument-41-audio.wav", 0.000000, 0.174000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent455 = insert(InstrumentAudioEvent.create(audio454, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords
    audio456 = insert(InstrumentAudio.create(instrument41, "Snare 22", "90b1fd35-cb74-4897-811a-578b4f628c9c-instrument-41-audio.wav", 0.000000, 0.227000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent457 = insert(InstrumentAudioEvent.create(audio456, 0.000000, 1.000000, "SNARE", "F#8", 1.000000));
    // Instrument Audio Chords
    audio458 = insert(InstrumentAudio.create(instrument41, "Kick 25", "06abcac4-6f12-4581-aa6e-d509354175ff-instrument-41-audio.wav", 0.002000, 201.000000, 121.000000, 106.904000, 0.600000));
    // Instrument Audio Events
    audioEvent459 = insert(InstrumentAudioEvent.create(audio458, 0.000000, 1.000000, "KICK", "A2", 1.000000));
    // Instrument Audio Chords
    audio460 = insert(InstrumentAudio.create(instrument41, "Snare 15", "4817ff3c-0bbc-472f-af44-6c91e139e426-instrument-41-audio.wav", 0.000000, 0.192000, 121.000000, 155.844000, 0.600000));
    // Instrument Audio Events
    audioEvent461 = insert(InstrumentAudioEvent.create(audio460, 0.000000, 1.000000, "SNARE", "D#3", 1.000000));
    // Instrument Audio Chords
    audio462 = insert(InstrumentAudio.create(instrument41, "Snare 12", "4e5d6a65-5597-457c-94d0-2a0de54d93ee-instrument-41-audio.wav", 0.000000, 0.233000, 121.000000, 545.455000, 0.600000));
    // Instrument Audio Events
    audioEvent463 = insert(InstrumentAudioEvent.create(audio462, 0.000000, 1.000000, "SNARE", "C#5", 1.000000));
    // Instrument Audio Chords
    audio464 = insert(InstrumentAudio.create(instrument41, "Kick 5", "22da4ceb-abd4-4d95-95b2-ea3d16c9d22a-instrument-41-audio.wav", 0.000000, 0.218000, 121.000000, 124.031000, 0.600000));
    // Instrument Audio Events
    audioEvent465 = insert(InstrumentAudioEvent.create(audio464, 0.000000, 1.000000, "KICK", "B2", 1.000000));
    // Instrument Audio Chords
    audio466 = insert(InstrumentAudio.create(instrument41, "Snare 23", "1fb23135-597b-4d3f-a808-5bb64494602d-instrument-41-audio.wav", 0.000000, 0.233000, 121.000000, 545.455000, 0.600000));
    // Instrument Audio Events
    audioEvent467 = insert(InstrumentAudioEvent.create(audio466, 0.000000, 1.000000, "SNARE", "C#5", 1.000000));
    // Instrument Audio Chords
    audio468 = insert(InstrumentAudio.create(instrument41, "Snare 30", "dc4b6e0e-fbd5-4d53-992c-f33598a6edf2-instrument-41-audio.wav", 0.000000, 0.244000, 121.000000, 4000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent469 = insert(InstrumentAudioEvent.create(audio468, 0.000000, 1.000000, "SNARE", "B7", 1.000000));
    // Instrument Audio Chords
    audio470 = insert(InstrumentAudio.create(instrument41, "Kick 2", "dcab4861-71ea-4c8d-8e21-11191c258b9e-instrument-41-audio.wav", 0.000000, 0.203000, 121.000000, 146.341000, 0.600000));
    // Instrument Audio Events
    audioEvent471 = insert(InstrumentAudioEvent.create(audio470, 0.000000, 1.000000, "KICK", "X", 1.000000));
    // Instrument Audio Chords
    audio472 = insert(InstrumentAudio.create(instrument41, "Kick 22", "1c671fcf-78e1-49e4-8835-f0b8c6f9d12b-instrument-41-audio.wav", 0.000000, 0.208000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent473 = insert(InstrumentAudioEvent.create(audio472, 0.000000, 1.000000, "KICK", "F#8", 1.000000));
    // Instrument Audio Chords
    audio474 = insert(InstrumentAudio.create(instrument41, "Kick 13", "af2a3a16-0663-4284-a9d7-6394c85c9c7b-instrument-41-audio.wav", 0.000000, 0.222000, 121.000000, 92.131000, 0.600000));
    // Instrument Audio Events
    audioEvent475 = insert(InstrumentAudioEvent.create(audio474, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords
    audio476 = insert(InstrumentAudio.create(instrument41, "Snare 24", "7cabf0f1-21bb-4242-b3f7-9bc31f676f7a-instrument-41-audio.wav", 0.003000, 0.251000, 121.000000, 259.459000, 0.600000));
    // Instrument Audio Events
    audioEvent477 = insert(InstrumentAudioEvent.create(audio476, 0.000000, 1.000000, "SNARE", "C4", 1.000000));
    // Instrument Audio Chords
    audio478 = insert(InstrumentAudio.create(instrument41, "Dun Dun Bell", "81fda5b3-33e5-48ac-9304-ead26b731275-instrument-28-audio.wav", 0.000500, 0.250000, 121.000000, 3428.570000, 0.600000));
    // Instrument Audio Events
    audioEvent479 = insert(InstrumentAudioEvent.create(audio478, 0.000000, 1.000000, "TOM", "A7", 1.000000));
    // Instrument Audio Chords
    audio480 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 4", "2966cd70-9351-4d10-95bd-ba0159e0487a-instrument-41-audio.wav", 0.000000, 0.194000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent481 = insert(InstrumentAudioEvent.create(audio480, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords
    audio482 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 26", "7662ad41-fb48-4b6b-bbb9-fbd2c8d6cc53-instrument-41-audio.wav", 0.000000, 0.197000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent483 = insert(InstrumentAudioEvent.create(audio482, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords
    audio484 = insert(InstrumentAudio.create(instrument41, "Snare 6", "ae26022f-aa30-4354-843f-d560e8791d65-instrument-41-audio.wav", 0.000000, 0.253000, 121.000000, 3000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent485 = insert(InstrumentAudioEvent.create(audio484, 0.000000, 1.000000, "SNARE", "F#7", 1.000000));
    // Instrument Audio Chords
    audio486 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 1", "db4da6b4-c9bb-44cb-8337-a7de9889bdf4-instrument-41-audio.wav", 0.000000, 0.160000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent487 = insert(InstrumentAudioEvent.create(audio486, 0.000000, 1.000000, "HIHAT", "D8", 1.000000));
    // Instrument Audio Chords
    audio488 = insert(InstrumentAudio.create(instrument41, "Kick 16", "48525cd6-778e-48e4-b7e3-a1d3878f0575-instrument-41-audio.wav", 0.000000, 0.214000, 121.000000, 107.623000, 0.600000));
    // Instrument Audio Events
    audioEvent489 = insert(InstrumentAudioEvent.create(audio488, 0.000000, 1.000000, "KICK", "A2", 1.000000));
    // Instrument Audio Chords
    audio490 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 12", "959c0f2e-513c-47ca-ae48-76b1bd9189a3-instrument-41-audio.wav", 0.000000, 0.151000, 121.000000, 213.333000, 0.600000));
    // Instrument Audio Events
    audioEvent491 = insert(InstrumentAudioEvent.create(audio490, 0.000000, 1.000000, "HIHAT", "G#3", 1.000000));
    // Instrument Audio Chords
    audio492 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 17", "61844405-ac3a-4880-ac15-25424af2a140-instrument-41-audio.wav", 0.003000, 0.169000, 121.000000, 432.432000, 0.600000));
    // Instrument Audio Events
    audioEvent493 = insert(InstrumentAudioEvent.create(audio492, 0.000000, 1.000000, "HIHAT", "A4", 1.000000));
    // Instrument Audio Chords
    audio494 = insert(InstrumentAudio.create(instrument41, "Powering Down", "1b6221ea-7f22-4845-b93f-50e901755c43-instrument-29-audio.wav", 0.000000, 2.000000, 121.000000, 4000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent495 = insert(InstrumentAudioEvent.create(audio494, 0.000000, 1.000000, "CRASH", "B7", 1.000000));
    // Instrument Audio Chords
    audio496 = insert(InstrumentAudio.create(instrument41, "Kick 26", "7339c5c5-78e1-4bbe-b2bd-e0eb7a42d940-instrument-41-audio.wav", 0.000000, 0.208000, 121.000000, 103.672000, 0.600000));
    // Instrument Audio Events
    audioEvent497 = insert(InstrumentAudioEvent.create(audio496, 0.000000, 1.000000, "KICK", "G#2", 1.000000));
    // Instrument Audio Chords
    audio498 = insert(InstrumentAudio.create(instrument41, "Kick 10", "cae8c01c-4dca-481a-823b-af333275a9e5-instrument-41-audio.wav", 0.000000, 0.218000, 121.000000, 115.942000, 0.600000));
    // Instrument Audio Events
    audioEvent499 = insert(InstrumentAudioEvent.create(audio498, 0.000000, 1.000000, "KICK", "A#2", 1.000000));
    // Instrument Audio Chords
    audio500 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 11", "250326c0-3d9a-436a-9b35-48c6960e8e90-instrument-41-audio.wav", 0.000000, 0.151000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent501 = insert(InstrumentAudioEvent.create(audio500, 0.000000, 1.000000, "HIHAT", "A8", 1.000000));
    // Instrument Audio Chords
    audio502 = insert(InstrumentAudio.create(instrument41, "Snare 16", "0605618c-f610-4909-89cc-8f94a8ce49c3-instrument-41-audio.wav", 0.000000, 0.241000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent503 = insert(InstrumentAudioEvent.create(audio502, 0.000000, 1.000000, "SNARE", "F#8", 1.000000));
    // Instrument Audio Chords
    audio504 = insert(InstrumentAudio.create(instrument41, "Snare 9", "9c716e11-5bc0-4862-8b4c-8e6a743de195-instrument-41-audio.wav", 0.003000, 0.228000, 121.000000, 3692.310000, 0.600000));
    // Instrument Audio Events
    audioEvent505 = insert(InstrumentAudioEvent.create(audio504, 0.000000, 1.000000, "SNARE", "A#7", 1.000000));
    // Instrument Audio Chords
    audio506 = insert(InstrumentAudio.create(instrument41, "Moog Snare 1", "be6cd4fc-9835-4ec3-9628-bbbae5655b69-instrument-29-audio.wav", 0.000000, 0.500000, 121.000000, 125.326000, 0.600000));
    // Instrument Audio Events
    audioEvent507 = insert(InstrumentAudioEvent.create(audio506, 0.000000, 1.000000, "SNARE", "B2", 1.000000));
    // Instrument Audio Chords
    audio508 = insert(InstrumentAudio.create(instrument41, "Kick 18", "503d2db2-50c2-4334-9b39-d5eeaf948446-instrument-41-audio.wav", 0.000000, 0.212000, 121.000000, 32.663000, 0.600000));
    // Instrument Audio Events
    audioEvent509 = insert(InstrumentAudioEvent.create(audio508, 0.000000, 1.000000, "KICK", "B1", 1.000000));
    // Instrument Audio Chords
    audio510 = insert(InstrumentAudio.create(instrument41, "Kick 23", "fac476ab-7f8b-41d1-a5c9-e3558db43705-instrument-41-audio.wav", 0.000000, 0.205000, 121.000000, 119.701000, 0.600000));
    // Instrument Audio Events
    audioEvent511 = insert(InstrumentAudioEvent.create(audio510, 0.000000, 1.000000, "KICK", "A#2", 1.000000));
    // Instrument Audio Chords
    audio512 = insert(InstrumentAudio.create(instrument41, "Kick 28", "c4e099a8-f4dc-4b4a-984b-9648e90802f1-instrument-41-audio.wav", 0.000000, 0.207000, 121.000000, 92.486000, 0.600000));
    // Instrument Audio Events
    audioEvent513 = insert(InstrumentAudioEvent.create(audio512, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords
    audio514 = insert(InstrumentAudio.create(instrument41, "Snare 19", "1881e57a-3570-4ea9-aa2b-c9602cfba10c-instrument-41-audio.wav", 0.000000, 0.233000, 121.000000, 181.132000, 0.600000));
    // Instrument Audio Events
    audioEvent515 = insert(InstrumentAudioEvent.create(audio514, 0.000000, 1.000000, "SNARE", "F#3", 1.000000));
    // Instrument Audio Chords
    audio516 = insert(InstrumentAudio.create(instrument41, "Clap 1", "37e75a56-abe2-4c97-b4c5-451f57c0ef33-instrument-41-audio.wav", 0.000000, 0.203000, 121.000000, 147.692000, 0.600000));
    // Instrument Audio Events
    audioEvent517 = insert(InstrumentAudioEvent.create(audio516, 0.000000, 1.000000, "SNARE", "D3", 1.000000));
    // Instrument Audio Chords
    audio518 = insert(InstrumentAudio.create(instrument41, "Kick 17", "1cba0fa2-8d99-4360-a060-1342e9168da8-instrument-41-audio.wav", 0.000000, 0.211000, 121.000000, 9600.000000, 0.600000));
    // Instrument Audio Events
    audioEvent519 = insert(InstrumentAudioEvent.create(audio518, 0.000000, 1.000000, "KICK", "D9", 1.000000));
    // Instrument Audio Chords
    audio520 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 8", "aeb18499-e64e-4efe-bf40-05c60d3bdac0-instrument-41-audio.wav", 0.002000, 0.157000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent521 = insert(InstrumentAudioEvent.create(audio520, 0.000000, 1.000000, "HIHAT", "A8", 1.000000));
    // Instrument Audio Chords
    audio522 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 5", "dbc6e5b0-08a3-4cea-a1ec-490fe88f2ef4-instrument-41-audio.wav", 0.002000, 0.184000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent523 = insert(InstrumentAudioEvent.create(audio522, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords
    audio524 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 21", "4402cd3f-23e5-4819-ac07-562eee6273a3-instrument-41-audio.wav", 0.000000, 0.160000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent525 = insert(InstrumentAudioEvent.create(audio524, 0.000000, 1.000000, "HIHAT", "A8", 1.000000));
    // Instrument Audio Chords
    audio526 = insert(InstrumentAudio.create(instrument41, "Kick 29 ", "3fb128b8-6d86-4391-b11f-3bbf1db2571f-instrument-41-audio.wav", 0.000000, 0.204000, 121.000000, 93.023000, 0.600000));
    // Instrument Audio Events
    audioEvent527 = insert(InstrumentAudioEvent.create(audio526, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords
    audio528 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 6", "77f06640-60be-4bf9-8594-e8290fe57887-instrument-41-audio.wav", 0.002000, 0.160000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent529 = insert(InstrumentAudioEvent.create(audio528, 0.000000, 1.000000, "HIHAT", "D8", 1.000000));
    // Instrument Audio Chords
    audio530 = insert(InstrumentAudio.create(instrument41, "Kick 6", "a4f218ef-54eb-4370-9cb2-e3532071548c-instrument-41-audio.wav", 0.000000, 0.223000, 121.000000, 90.566000, 0.600000));
    // Instrument Audio Events
    audioEvent531 = insert(InstrumentAudioEvent.create(audio530, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords
    audio532 = insert(InstrumentAudio.create(instrument41, "Snare 2", "c56ba0e8-2bf8-4368-bb95-d130c884fb7e-instrument-41-audio.wav", 0.000000, 0.199000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent533 = insert(InstrumentAudioEvent.create(audio532, 0.000000, 1.000000, "SNARE", "A8", 1.000000));
    // Instrument Audio Chords
    audio534 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 2", "eee26dc9-3be2-4136-bc2d-6c713d335b74-instrument-41-audio.wav", 0.000000, 0.193000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent535 = insert(InstrumentAudioEvent.create(audio534, 0.000000, 1.000000, "HIHAT", "D8", 1.000000));
    // Instrument Audio Chords
    audio536 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 14", "8b534f40-794e-4683-a4ed-4eb4eb091516-instrument-41-audio.wav", 0.000000, 0.181000, 121.000000, 979.592000, 0.600000));
    // Instrument Audio Events
    audioEvent537 = insert(InstrumentAudioEvent.create(audio536, 0.000000, 1.000000, "HIHAT", "B5", 1.000000));
    // Instrument Audio Chords
    audio538 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 18", "631a315c-656c-45b5-90bc-f55022dd6e8e-instrument-41-audio.wav", 0.003000, 0.166000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent539 = insert(InstrumentAudioEvent.create(audio538, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords
    audio540 = insert(InstrumentAudio.create(instrument41, "Kick 8", "4582c30c-6572-4e69-812c-712ed06656c0-instrument-41-audio.wav", 0.000000, 0.217000, 121.000000, 79.689000, 0.600000));
    // Instrument Audio Events
    audioEvent541 = insert(InstrumentAudioEvent.create(audio540, 0.000000, 1.000000, "KICK", "D#2", 1.000000));
    // Instrument Audio Chords
    audio542 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 28", "ea4ac717-55a9-4c15-b376-cc73bded01a9-instrument-41-audio.wav", 0.000000, 0.185000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent543 = insert(InstrumentAudioEvent.create(audio542, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords
    audio544 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 19", "00c84fb5-4097-4417-80e1-cf9b973961a8-instrument-41-audio.wav", 0.000000, 0.165000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent545 = insert(InstrumentAudioEvent.create(audio544, 0.000000, 4.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio546 = insert(InstrumentAudio.create(instrument41, "Clap 3", "1046032b-22d5-4fc8-85b4-48fda751c395-instrument-41-audio.wav", 0.000000, 0.304000, 121.000000, 623.377000, 0.600000));
    // Instrument Audio Events
    audioEvent547 = insert(InstrumentAudioEvent.create(audio546, 0.000000, 1.000000, "SNARE", "D#5", 1.000000));
    // Instrument Audio Chords
    audio548 = insert(InstrumentAudio.create(instrument41, "Snare 7", "0b5594dc-b9d8-4e1c-88b0-945640ba3086-instrument-41-audio.wav", 0.000000, 0.193000, 121.000000, 2823.530000, 0.600000));
    // Instrument Audio Events
    audioEvent549 = insert(InstrumentAudioEvent.create(audio548, 0.000000, 1.000000, "SNARE", "F7", 1.000000));
    // Instrument Audio Chords
    audio550 = insert(InstrumentAudio.create(instrument41, "Kick 11 ", "85dde3af-9c8d-443f-b70e-af5a8377a265-instrument-41-audio.wav", 0.000000, 0.211000, 121.000000, 115.108000, 0.600000));
    // Instrument Audio Events
    audioEvent551 = insert(InstrumentAudioEvent.create(audio550, 0.000000, 1.000000, "KICK", "A#2", 1.000000));
    // Instrument Audio Chords
    audio552 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 24", "02e9ccc3-def9-4c25-8c2a-676862166b80-instrument-41-audio.wav", 0.002000, 0.180000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent553 = insert(InstrumentAudioEvent.create(audio552, 0.000000, 1.000000, "HIHAT", "D8", 1.000000));
    // Instrument Audio Chords
    audio554 = insert(InstrumentAudio.create(instrument41, "Kick 15", "042c9206-1d80-489d-80d7-acb4952801c7-instrument-41-audio.wav", 0.000000, 0.224000, 121.000000, 134.454000, 0.600000));
    // Instrument Audio Events
    audioEvent555 = insert(InstrumentAudioEvent.create(audio554, 0.000000, 1.000000, "KICK", "C3", 1.000000));
    // Instrument Audio Chords
    audio556 = insert(InstrumentAudio.create(instrument41, "Snare 1", "1b0d2ff8-1ba6-4302-bfd2-475f2792888f-instrument-41-audio.wav", 0.000000, 0.261000, 121.000000, 3000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent557 = insert(InstrumentAudioEvent.create(audio556, 0.000000, 1.000000, "SNARE", "F#7", 1.000000));
    // Instrument Audio Chords
    audio558 = insert(InstrumentAudio.create(instrument41, "Kick 12", "140411f4-21ed-4114-b9d3-b83ba4cd605d-instrument-41-audio.wav", 0.000000, 0.216000, 121.000000, 101.911000, 0.600000));
    // Instrument Audio Events
    audioEvent559 = insert(InstrumentAudioEvent.create(audio558, 0.000000, 1.000000, "KICK", "G#2", 1.000000));
    // Instrument Audio Chords
    audio560 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 9", "a4bb0dc5-3ee8-473b-a91b-8be7ef0f2a06-instrument-41-audio.wav", 0.000000, 0.160000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent561 = insert(InstrumentAudioEvent.create(audio560, 0.000000, 1.000000, "HIHAT", "F#8", 1.000000));
    // Instrument Audio Chords
    audio562 = insert(InstrumentAudio.create(instrument41, "Dun Dun Da Bell", "fad125f5-b8a1-449d-acad-24ac69d74043-instrument-28-audio.wav", 0.000000, 0.250000, 121.000000, 1600.000000, 0.600000));
    // Instrument Audio Events
    audioEvent563 = insert(InstrumentAudioEvent.create(audio562, 0.000000, 1.000000, "TOM", "G6", 1.000000));
    // Instrument Audio Chords
    audio564 = insert(InstrumentAudio.create(instrument41, "Snare 29", "f3d00a5d-132a-45c7-ad14-d3a5d7714754-instrument-41-audio.wav", 0.000000, 0.244000, 121.000000, 3200.000000, 0.600000));
    // Instrument Audio Events
    audioEvent565 = insert(InstrumentAudioEvent.create(audio564, 0.000000, 1.000000, "SNARE", "G7", 1.000000));
    // Instrument Audio Chords
    audio566 = insert(InstrumentAudio.create(instrument41, "Kick 27", "9e1e3aae-40ff-44cb-a94e-66ce9c7c436b-instrument-41-audio.wav", 0.000000, 0.211000, 121.000000, 106.904000, 0.600000));
    // Instrument Audio Events
    audioEvent567 = insert(InstrumentAudioEvent.create(audio566, 0.000000, 1.000000, "KICK", "A2", 1.000000));
    // Instrument Audio Chords
    audio568 = insert(InstrumentAudio.create(instrument41, "Snare 27", "c7fbafd1-2aa3-480e-b0e3-b16189e90e11-instrument-41-audio.wav", 0.000000, 0.235000, 121.000000, 4000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent569 = insert(InstrumentAudioEvent.create(audio568, 0.000000, 1.000000, "SNARE", "B7", 1.000000));
    // Instrument Audio Chords
    audio570 = insert(InstrumentAudio.create(instrument41, "Snare 21", "08068837-646b-4b2a-a3ee-f091ed9c5ae7-instrument-41-audio.wav", 0.000000, 0.217000, 121.000000, 539.326000, 0.600000));
    // Instrument Audio Events
    audioEvent571 = insert(InstrumentAudioEvent.create(audio570, 0.000000, 1.000000, "SNARE", "C#5", 1.000000));
    // Instrument Audio Chords
    audio572 = insert(InstrumentAudio.create(instrument41, "Kick 20", "b6fbe757-5f87-4120-b980-04ff29e21874-instrument-41-audio.wav", 0.000000, 0.208000, 121.000000, 107.143000, 0.600000));
    // Instrument Audio Events
    audioEvent573 = insert(InstrumentAudioEvent.create(audio572, 0.000000, 1.000000, "KICK", "A2", 1.000000));
    // Instrument Audio Chords
    audio574 = insert(InstrumentAudio.create(instrument41, "Clap 4", "7528ee6d-ec2a-42e4-9098-6ead2e829836-instrument-41-audio.wav", 0.000000, 0.312000, 121.000000, 489.796000, 0.600000));
    // Instrument Audio Events
    audioEvent575 = insert(InstrumentAudioEvent.create(audio574, 0.000000, 1.000000, "SNARE", "B4", 1.000000));
    // Instrument Audio Chords
    audio576 = insert(InstrumentAudio.create(instrument41, "Hi-hat 29", "250c412f-8166-4162-b735-5cdac72a6ffb-instrument-41-audio.wav", 0.000000, 0.173000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent577 = insert(InstrumentAudioEvent.create(audio576, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords
    audio578 = insert(InstrumentAudio.create(instrument41, "Snare 10", "bfddbe02-57c7-4a2f-a82d-57a26c5e0324-instrument-41-audio.wav", 0.002000, 0.313000, 121.000000, 4000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent579 = insert(InstrumentAudioEvent.create(audio578, 0.000000, 1.000000, "SNARE", "B7", 1.000000));
    // Instrument Audio Chords
    audio580 = insert(InstrumentAudio.create(instrument41, "Clap 2", "99862afa-7e85-4513-a54f-87daa94bdb4f-instrument-41-audio.wav", 0.000000, 0.291000, 121.000000, 615.385000, 0.600000));
    // Instrument Audio Events
    audioEvent581 = insert(InstrumentAudioEvent.create(audio580, 0.000000, 1.000000, "SNARE", "D#5", 1.000000));
    // Instrument Audio Chords
    audio582 = insert(InstrumentAudio.create(instrument41, "Rough and Sandy Crash", "6f440597-40e8-4d9d-8855-6d889c827df6-instrument-29-audio.wav", 0.000200, 2.000000, 121.000000, 1230.770000, 0.600000));
    // Instrument Audio Events
    audioEvent583 = insert(InstrumentAudioEvent.create(audio582, 0.000000, 1.000000, "CRASH", "Eb6", 1.000000));
    // Instrument Audio Chords
    audio584 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 22", "aec26741-82e2-4e42-a165-ae7156bde742-instrument-41-audio.wav", 0.002000, 0.174000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent585 = insert(InstrumentAudioEvent.create(audio584, 0.000000, 1.000000, "HIHAT", "A8", 1.000000));
    // Instrument Audio Chords
    audio586 = insert(InstrumentAudio.create(instrument41, "Snare 3", "7d2cc6d0-9bd9-48e7-9282-5ea0c29bb427-instrument-41-audio.wav", 0.002000, 0.241000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent587 = insert(InstrumentAudioEvent.create(audio586, 0.000000, 1.000000, "SNARE", "D8", 1.000000));
    // Instrument Audio Chords
    audio588 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 30", "8699e3b8-0a91-42d7-bac3-ff4a187c2845-instrument-41-audio.wav", 0.003000, 0.178000, 121.000000, 551.724000, 0.600000));
    // Instrument Audio Events
    audioEvent589 = insert(InstrumentAudioEvent.create(audio588, 0.000000, 1.000000, "HIHAT", "C#5", 1.000000));
    // Instrument Audio Chords
    audio590 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 13", "3fb72846-001a-424f-8d9f-ff26cc119561-instrument-41-audio.wav", 0.000000, 0.167000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent591 = insert(InstrumentAudioEvent.create(audio590, 0.000000, 1.000000, "HIHAT", "A8", 1.000000));
    // Instrument Audio Chords
    audio592 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 25", "c04f53dd-05f2-448a-b52c-fafac8e16821-instrument-41-audio.wav", 0.000000, 0.177000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent593 = insert(InstrumentAudioEvent.create(audio592, 0.000000, 1.000000, "HIHAT", "F#8", 1.000000));
    // Instrument Audio Chords
    audio594 = insert(InstrumentAudio.create(instrument41, "Clap 5", "681699f9-a91c-4222-93bd-f6ee5da4adf4-instrument-41-audio.wav", 0.000000, 0.267000, 121.000000, 91.603000, 0.600000));
    // Instrument Audio Events
    audioEvent595 = insert(InstrumentAudioEvent.create(audio594, 0.000000, 1.000000, "SNARE", "F#2", 1.000000));
    // Instrument Audio Chords
    audio596 = insert(InstrumentAudio.create(instrument41, "Sangpan Bell", "bac419d8-2391-4b62-b45c-71122f8c2df6-instrument-28-audio.wav", 0.000100, 0.250000, 121.000000, 615.385000, 0.600000));
    // Instrument Audio Events
    audioEvent597 = insert(InstrumentAudioEvent.create(audio596, 0.000000, 1.000000, "TOM", "Eb5", 1.000000));
    // Instrument Audio Chords
    audio598 = insert(InstrumentAudio.create(instrument41, "Clap 7", "e5c563cd-26da-4513-8f8f-0c980d26baba-instrument-41-audio.wav", 0.000000, 0.253000, 121.000000, 352.941000, 0.600000));
    // Instrument Audio Events
    audioEvent599 = insert(InstrumentAudioEvent.create(audio598, 0.000000, 1.000000, "SNARE", "F4", 1.000000));
    // Instrument Audio Chords
    audio600 = insert(InstrumentAudio.create(instrument41, "Snare 14", "0b0ffd9d-cfce-4dde-a86c-9d66a79b0713-instrument-41-audio.wav", 0.000000, 0.271000, 121.000000, 4363.640000, 0.600000));
    // Instrument Audio Events
    audioEvent601 = insert(InstrumentAudioEvent.create(audio600, 0.000000, 1.000000, "SNARE", "C#8", 1.000000));
    // Instrument Audio Chords
    audio602 = insert(InstrumentAudio.create(instrument41, "Kick 24", "ac9d8ca6-5c82-4956-9886-09915d0ac033-instrument-41-audio.wav", 0.000000, 0.208000, 121.000000, 105.960000, 0.600000));
    // Instrument Audio Events
    audioEvent603 = insert(InstrumentAudioEvent.create(audio602, 0.000000, 1.000000, "KICK", "G#2", 1.000000));
    // Instrument Audio Chords
    audio604 = insert(InstrumentAudio.create(instrument41, "Snare 12", "786f2937-9963-43d7-83f7-6b5f10fb5e06-instrument-41-audio.wav", 0.000000, 0.294000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent605 = insert(InstrumentAudioEvent.create(audio604, 0.000000, 1.000000, "SNARE", "B8", 1.000000));
    // Instrument Audio Chords
    audio606 = insert(InstrumentAudio.create(instrument41, "Kick 9", "d84cf238-eb4e-4c96-8b75-958bddf182cf-instrument-41-audio.wav", 0.000000, 0.218000, 121.000000, 67.989000, 0.600000));
    // Instrument Audio Events
    audioEvent607 = insert(InstrumentAudioEvent.create(audio606, 0.000000, 1.000000, "KICK", "C#2", 1.000000));
    // Instrument Audio Chords
    audio608 = insert(InstrumentAudio.create(instrument41, "Snare 4", "dcf54dc4-8b1f-44d8-bdcb-88f61d2c7bc8-instrument-41-audio.wav", 0.002000, 0.236000, 121.000000, 2823.530000, 0.600000));
    // Instrument Audio Events
    audioEvent609 = insert(InstrumentAudioEvent.create(audio608, 0.000000, 1.000000, "SNARE", "F7", 1.000000));
    // Instrument Audio Chords
    audio610 = insert(InstrumentAudio.create(instrument41, "Moog Snare 3", "a997bca3-4830-407a-b787-e2a569304a88-instrument-29-audio.wav", 0.000000, 0.500000, 121.000000, 307.692000, 0.600000));
    // Instrument Audio Events
    audioEvent611 = insert(InstrumentAudioEvent.create(audio610, 0.000000, 1.000000, "SNARE", "Eb4", 1.000000));
    // Instrument Audio Chords
    audio612 = insert(InstrumentAudio.create(instrument41, "Moog Snare 2", "dda386d5-d093-428a-a39c-78496d2d7ff3-instrument-29-audio.wav", 0.000000, 0.375000, 121.000000, 139.535000, 0.600000));
    // Instrument Audio Events
    audioEvent613 = insert(InstrumentAudioEvent.create(audio612, 0.000000, 1.000000, "SNARE", "C#3", 1.000000));
    // Instrument Audio Chords
    audio614 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 20", "7e79d46d-c7f3-4dff-a704-55052a9384ff-instrument-41-audio.wav", 0.000000, 0.180000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent615 = insert(InstrumentAudioEvent.create(audio614, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords
    audio616 = insert(InstrumentAudio.create(instrument41, "Kick 4", "76c48f70-1b35-4f9c-99b8-c795eeb00488-instrument-41-audio.wav", 0.000000, 0.226000, 121.000000, 79.867000, 0.600000));
    // Instrument Audio Events
    audioEvent617 = insert(InstrumentAudioEvent.create(audio616, 0.000000, 1.000000, "KICK", "D#2", 1.000000));
    // Instrument Audio Chords
    audio618 = insert(InstrumentAudio.create(instrument41, "Snare 28", "10d47dd8-8832-4dcf-b1f9-312b9af1e03c-instrument-41-audio.wav", 0.002000, 0.215000, 121.000000, 234.146000, 0.600000));
    // Instrument Audio Events
    audioEvent619 = insert(InstrumentAudioEvent.create(audio618, 0.000000, 1.000000, "SNARE", "A#3", 1.000000));
    // Instrument Audio Chords
    audio620 = insert(InstrumentAudio.create(instrument41, "Kick 14", "7a5312d6-844d-4d9b-97bb-40c15e7294ec-instrument-41-audio.wav", 0.000000, 0.212000, 121.000000, 108.597000, 0.600000));
    // Instrument Audio Events
    audioEvent621 = insert(InstrumentAudioEvent.create(audio620, 0.000000, 1.000000, "KICK", "A2", 1.000000));
    // Instrument Audio Chords
    audio622 = insert(InstrumentAudio.create(instrument41, "Snare 8", "f1ae646f-a217-4475-860c-81f5349b6fd8-instrument-41-audio.wav", 0.000000, 0.203000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent623 = insert(InstrumentAudioEvent.create(audio622, 0.000000, 1.000000, "SNARE", "F#8", 1.000000));
    // Instrument Audio Chords
    audio624 = insert(InstrumentAudio.create(instrument41, "Kick 1 ", "0ca7c617-152f-4a76-83fd-fbfca83d239f-instrument-41-audio.wav", 0.000000, 0.204000, 121.000000, 92.644000, 0.600000));
    // Instrument Audio Events
    audioEvent625 = insert(InstrumentAudioEvent.create(audio624, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords
    audio626 = insert(InstrumentAudio.create(instrument41, "Snare 25", "aada609c-cd8c-4509-aa4f-8482accc280d-instrument-41-audio.wav", 0.000000, 0.230000, 121.000000, 173.285000, 0.600000));
    // Instrument Audio Events
    audioEvent627 = insert(InstrumentAudioEvent.create(audio626, 0.000000, 1.000000, "SNARE", "F3", 1.000000));
    // Instrument Audio Chords
    audio628 = insert(InstrumentAudio.create(instrument41, "Snare 5", "dab989e1-3afc-4a77-8e6d-e2ca8599882e-instrument-41-audio.wav", 0.002000, 0.276000, 121.000000, 117.073000, 0.600000));
    // Instrument Audio Events
    audioEvent629 = insert(InstrumentAudioEvent.create(audio628, 0.000000, 1.000000, "SNARE", "A#2", 1.000000));
    // Instrument Audio Chords
    audio630 = insert(InstrumentAudio.create(instrument41, "Kick 3", "73335892-500e-42e2-ad00-6261c80ab41a-instrument-41-audio.wav", 0.000000, 0.200000, 121.000000, 106.904000, 0.600000));
    // Instrument Audio Events
    audioEvent631 = insert(InstrumentAudioEvent.create(audio630, 0.000000, 1.000000, "KICK", "A2", 1.000000));
    // Instrument Audio Chords
    audio632 = insert(InstrumentAudio.create(instrument41, "Snare 13", "9ea773e2-450e-464a-a204-e83c80df43ee-instrument-41-audio.wav", 0.002000, 0.304000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent633 = insert(InstrumentAudioEvent.create(audio632, 0.000000, 1.000000, "SNARE", "A8", 1.000000));
    // Instrument Audio Chords
    audio634 = insert(InstrumentAudio.create(instrument41, "Snare 26", "3ffdc7b4-5b5e-445a-ab99-bb57aa34071a-instrument-41-audio.wav", 0.000000, 0.228000, 121.000000, 220.183000, 0.600000));
    // Instrument Audio Events
    audioEvent635 = insert(InstrumentAudioEvent.create(audio634, 0.000000, 1.000000, "SNARE", "A3", 1.000000));
    // Instrument Audio Chords
    audio636 = insert(InstrumentAudio.create(instrument41, "Clap 6", "eaed6811-bd77-4d1f-8da6-cf4f7a44dd65-instrument-41-audio.wav", 0.000000, 0.295000, 121.000000, 298.137000, 0.600000));
    // Instrument Audio Events
    audioEvent637 = insert(InstrumentAudioEvent.create(audio636, 0.000000, 1.000000, "SNARE", "D4", 1.000000));
    // Instrument Audio Chords
    audio638 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 10", "3e71c193-a0e4-42f5-90d0-0218454080ac-instrument-41-audio.wav", 0.000000, 0.174000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent639 = insert(InstrumentAudioEvent.create(audio638, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords
    audio640 = insert(InstrumentAudio.create(instrument41, "Crash 7", "6ff38a9c-5bc5-43be-ae83-5e2ceb42abf2-instrument-41-audio.wav", 0.091000, 0.980000, 121.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent641 = insert(InstrumentAudioEvent.create(audio640, 0.000000, 2.000000, "CRASH", "F#6", 1.000000));
    // Instrument Audio Chords
    audio642 = insert(InstrumentAudio.create(instrument41, "Kick 7 ", "4edb7554-3e22-414c-84b5-6192e87269e0-instrument-41-audio.wav", 0.000000, 0.222000, 121.000000, 80.000000, 0.600000));
    // Instrument Audio Events
    audioEvent643 = insert(InstrumentAudioEvent.create(audio642, 0.000000, 1.000000, "KICK", "D#2", 1.000000));
    // Instrument Audio Chords
    audio644 = insert(InstrumentAudio.create(instrument41, "Snare 11 ", "08e1ae69-84c5-4b12-ac6e-9e71ead2ac2d-instrument-41-audio.wav", 0.001000, 0.287000, 121.000000, 224.299000, 0.600000));
    // Instrument Audio Events
    audioEvent645 = insert(InstrumentAudioEvent.create(audio644, 0.000000, 1.000000, "SNARE", "A3", 1.000000));
    // Instrument Audio Chords
    audio646 = insert(InstrumentAudio.create(instrument41, "Kick 21", "8de3182d-786f-45e2-bf89-266808fbe246-instrument-41-audio.wav", 0.000000, 0.202000, 121.000000, 116.223000, 0.600000));
    // Instrument Audio Events
    audioEvent647 = insert(InstrumentAudioEvent.create(audio646, 0.000000, 1.000000, "KICK", "A#2", 1.000000));
    // Instrument Audio Chords
    audio648 = insert(InstrumentAudio.create(instrument41, "Kick 2", "b5e88de7-dbed-4d4c-bb95-9cd932eb4024-instrument-41-audio.wav", 0.000000, 0.203000, 121.000000, 146.341000, 0.600000));
    // Instrument Audio Events
    audioEvent649 = insert(InstrumentAudioEvent.create(audio648, 0.000000, 1.000000, "KICK", "X", 1.000000));
    // Instrument Audio Chords
    audio650 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 23", "9406016b-0a60-4377-8fd9-50f33a698c6b-instrument-41-audio.wav", 0.002000, 0.210000, 121.000000, 521.739000, 0.600000));
    // Instrument Audio Events
    audioEvent651 = insert(InstrumentAudioEvent.create(audio650, 0.000000, 1.000000, "HIHAT", "C5", 1.000000));
    // Instrument Audio Chords
    audio652 = insert(InstrumentAudio.create(instrument41, "Snare 20", "197bcc44-a886-4c42-b1f1-ed606eb9e98f-instrument-41-audio.wav", 0.000000, 0.204000, 121.000000, 1230.770000, 0.600000));
    // Instrument Audio Events
    audioEvent653 = insert(InstrumentAudioEvent.create(audio652, 0.000000, 1.000000, "SNARE", "D#6", 1.000000));
    // Instrument Audio Chords
    audio654 = insert(InstrumentAudio.create(instrument41, "Snare 18", "ff903ce7-36d7-4811-a9f9-22c754b7fa45-instrument-41-audio.wav", 0.000000, 0.234000, 121.000000, 4000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent655 = insert(InstrumentAudioEvent.create(audio654, 0.000000, 1.000000, "SNARE", "B7", 1.000000));
    // Instrument Audio Chords
    audio656 = insert(InstrumentAudio.create(instrument41, "Tight Acoustic Snare", "e1f9f407-41a9-4f71-b175-8ef43d38cbe9-instrument-29-audio.wav", 0.000000, 0.375000, 121.000000, 3692.310000, 0.600000));
    // Instrument Audio Events
    audioEvent657 = insert(InstrumentAudioEvent.create(audio656, 0.000000, 1.000000, "SNARE", "Bb7", 1.000000));
    // Instrument Audio Chords
    audio658 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 27", "72b37477-b0dc-4ccf-8efc-1d569ef58692-instrument-41-audio.wav", 0.000000, 0.173000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent659 = insert(InstrumentAudioEvent.create(audio658, 0.000000, 1.000000, "HIHAT", "B8", 1.000000));
    // Instrument Audio Chords
    audio660 = insert(InstrumentAudio.create(instrument41, "Snare 17", "006a2eea-9f8d-4f7a-9594-bee83e5fbd4b-instrument-41-audio.wav", 0.000000, 0.241000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent661 = insert(InstrumentAudioEvent.create(audio660, 0.000000, 1.000000, "SNARE", "A8", 1.000000));
    // Instrument Audio Chords
    audio662 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 7", "dea07cbc-c686-4030-b791-cb2f6957082e-instrument-41-audio.wav", 0.000000, 0.171000, 121.000000, 4000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent663 = insert(InstrumentAudioEvent.create(audio662, 0.000000, 1.000000, "HIHAT", "B7", 1.000000));
    // Instrument Audio Chords
    audio664 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 15", "7b8a6720-3b52-4ccd-a7ff-b4bb2aa241d4-instrument-41-audio.wav", 0.000000, 0.148000, 121.000000, 480.000000, 0.600000));
    // Instrument Audio Events
    audioEvent665 = insert(InstrumentAudioEvent.create(audio664, 0.000000, 1.000000, "HIHAT", "B4", 1.000000));
    // Instrument Audio Chords
    audio666 = insert(InstrumentAudio.create(instrument41, "Kick 19", "a4a389c5-9645-48df-a054-7d04d59b45a8-instrument-41-audio.wav", 0.000000, 0.206000, 121.000000, 94.118000, 0.600000));
    // Instrument Audio Events
    audioEvent667 = insert(InstrumentAudioEvent.create(audio666, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords
    audio668 = insert(InstrumentAudio.create(instrument41, "Hi-Hat 16", "c8236068-8da4-45d8-9dea-55085a9553fd-instrument-41-audio.wav", 0.000000, 0.166000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent669 = insert(InstrumentAudioEvent.create(audio668, 0.000000, 1.000000, "HIHAT", "A8", 1.000000));
    // Instrument Audio Chords

  }

  private void go6() throws CoreException {
    // Insert Percussive-type Instrument Fire Small
    instrument29 = insert(Instrument.create(user3, library3, "Percussive", "Published", "Fire Small", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument29, "Fire"));
    insert(InstrumentMeme.create(instrument29, "Small"));
    // Instrument Audios
    audio670 = insert(InstrumentAudio.create(instrument29, "Open To Closed Hat", "979f0b39-c642-4f2d-b31b-f9dd327f6f94-instrument-29-audio.wav", 0.001500, 0.328000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent671 = insert(InstrumentAudioEvent.create(audio670, 0.000000, 1.000000, "HIHATOPEN", "D8", 1.000000));
    // Instrument Audio Chords
    audio672 = insert(InstrumentAudio.create(instrument29, "Crunchy Snare", "c5d6a0cb-e103-417b-b450-77eb355099a3-instrument-29-audio.wav", 0.000200, 0.375000, 121.000000, 3200.000000, 0.600000));
    // Instrument Audio Events
    audioEvent673 = insert(InstrumentAudioEvent.create(audio672, 0.000000, 1.000000, "SNARE", "G7", 1.000000));
    // Instrument Audio Chords
    audio674 = insert(InstrumentAudio.create(instrument29, "Small Snare", "9bf9e365-c11a-4dd6-80a0-b2c2b4fa5d28-instrument-29-audio.wav", 0.000000, 0.250000, 121.000000, 282.353000, 0.600000));
    // Instrument Audio Events
    audioEvent675 = insert(InstrumentAudioEvent.create(audio674, 0.000000, 1.000000, "SNARE", "C#4", 1.000000));
    // Instrument Audio Chords
    audio676 = insert(InstrumentAudio.create(instrument29, "Closed Hat 2", "effafa95-9756-4119-84b8-4452fb00b160-instrument-29-audio.wav", 0.000200, 0.188000, 121.000000, 666.667000, 0.600000));
    // Instrument Audio Events
    audioEvent677 = insert(InstrumentAudioEvent.create(audio676, 0.000000, 1.000000, "HIHAT", "E5", 1.000000));
    // Instrument Audio Chords
    audio678 = insert(InstrumentAudio.create(instrument29, "Moog Snare 4", "91944422-ddab-48e6-b152-627afa877399-instrument-29-audio.wav", 0.000000, 0.250000, 121.000000, 137.931000, 0.600000));
    // Instrument Audio Events
    audioEvent679 = insert(InstrumentAudioEvent.create(audio678, 0.000000, 1.000000, "SNARE", "C#3", 1.000000));
    // Instrument Audio Chords
    audio680 = insert(InstrumentAudio.create(instrument29, "Industrial Undulating Percussion", "a3c983f4-b471-4603-8a94-25207a783056-instrument-29-audio.wav", 0.000000, 0.625000, 121.000000, 126.649000, 0.600000));
    // Instrument Audio Events
    audioEvent681 = insert(InstrumentAudioEvent.create(audio680, 0.000000, 1.000000, "KICK", "B2", 1.000000));
    // Instrument Audio Chords
    audio682 = insert(InstrumentAudio.create(instrument29, "Crash 16", "a3706dbd-4518-4a8e-a354-32e12f44bf78-instrument-29-audio.wav", 0.012000, 1.926000, 121.000000, 1225.000000, 0.600000));
    // Instrument Audio Events
    audioEvent683 = insert(InstrumentAudioEvent.create(audio682, 0.000000, 4.000000, "CRASH", "D#6", 1.000000));
    // Instrument Audio Chords
    audio684 = insert(InstrumentAudio.create(instrument29, "Muted Explosive Kick", "48a784a4-a36a-41ab-ac21-e8738e6a2c54-instrument-29-audio.wav", 0.003000, 0.375000, 121.000000, 86.799000, 0.600000));
    // Instrument Audio Events
    audioEvent685 = insert(InstrumentAudioEvent.create(audio684, 0.000000, 1.000000, "KICK", "F2", 1.000000));
    // Instrument Audio Chords
    audio686 = insert(InstrumentAudio.create(instrument29, "Snappy Snare", "c9384a2c-a4fc-4b2f-8ea5-50069b4c3988-instrument-29-audio.wav", 0.000100, 0.375000, 121.000000, 1714.290000, 0.600000));
    // Instrument Audio Events
    audioEvent687 = insert(InstrumentAudioEvent.create(audio686, 0.000000, 1.000000, "SNARE", "A6", 1.000000));
    // Instrument Audio Chords
    audio688 = insert(InstrumentAudio.create(instrument29, "Snappy Snare 3", "7f17ecca-4e1e-4177-bed5-3f5bcf273d65-instrument-29-audio.wav", 0.014400, 0.375000, 121.000000, 9600.000000, 0.600000));
    // Instrument Audio Events
    audioEvent689 = insert(InstrumentAudioEvent.create(audio688, 0.000000, 1.000000, "SNARE", "D9", 1.000000));
    // Instrument Audio Chords
    audio690 = insert(InstrumentAudio.create(instrument29, "Open Industrial Kick", "8d42e8e8-1286-4e71-b537-ceea1d925934-instrument-29-audio.wav", 0.003500, 0.594000, 121.000000, 80.402000, 0.600000));
    // Instrument Audio Events
    audioEvent691 = insert(InstrumentAudioEvent.create(audio690, 0.000000, 1.000000, "KICK", "E2", 1.000000));
    // Instrument Audio Chords
    audio692 = insert(InstrumentAudio.create(instrument29, "Crash 5", "12aef4fb-9f24-4f60-b39c-0388cdcdf1da-instrument-29-audio.wav", 0.022000, 4.157000, 121.000000, 4900.000000, 0.600000));
    // Instrument Audio Events
    audioEvent693 = insert(InstrumentAudioEvent.create(audio692, 0.000000, 4.000000, "CRASH", "D#5", 1.000000));
    // Instrument Audio Chords
    audio694 = insert(InstrumentAudio.create(instrument29, "CLOSED HAT 5", "5c32dc94-496d-412d-b4db-4839d905b074-instrument-29-audio.wav", 0.000100, 0.375000, 121.000000, 5333.330000, 0.600000));
    // Instrument Audio Events
    audioEvent695 = insert(InstrumentAudioEvent.create(audio694, 0.000000, 1.000000, "HIHAT", "E8", 1.000000));
    // Instrument Audio Chords
    audio696 = insert(InstrumentAudio.create(instrument29, "Closed Hat 3", "8722e12a-c1bb-405f-b9da-c90f1c10fed0-instrument-29-audio.wav", 0.000000, 0.188000, 121.000000, 393.443000, 0.600000));
    // Instrument Audio Events
    audioEvent697 = insert(InstrumentAudioEvent.create(audio696, 0.000000, 1.000000, "HIHAT", "G4", 1.000000));
    // Instrument Audio Chords
    audio698 = insert(InstrumentAudio.create(instrument29, "Tom 2", "38adc853-2272-496f-b129-5805b9226a21-instrument-29-audio.wav", 0.000000, 0.375000, 121.000000, 134.078000, 0.600000));
    // Instrument Audio Events
    audioEvent699 = insert(InstrumentAudioEvent.create(audio698, 0.000000, 1.000000, "TOM", "C3", 1.000000));
    // Instrument Audio Chords
    audio700 = insert(InstrumentAudio.create(instrument29, "Open Hat 1", "886c3d7c-7ad3-4c7d-a694-7e3e76671538-instrument-29-audio.wav", 0.000200, 0.328000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent701 = insert(InstrumentAudioEvent.create(audio700, 0.000000, 1.000000, "HIHATOPEN", "D8", 1.000000));
    // Instrument Audio Chords
    audio702 = insert(InstrumentAudio.create(instrument29, "Tom 1", "203c9a5f-6d89-4072-b1d6-77c36bfe151d-instrument-29-audio.wav", 0.000000, 0.250000, 121.000000, 167.832000, 0.600000));
    // Instrument Audio Events
    audioEvent703 = insert(InstrumentAudioEvent.create(audio702, 0.000000, 1.000000, "TOM", "E3", 1.000000));
    // Instrument Audio Chords
    audio704 = insert(InstrumentAudio.create(instrument29, "Flare Up Snare", "5575ef02-03a6-458c-b0bb-a5c975819b7a-instrument-29-audio.wav", 0.004800, 0.375000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent705 = insert(InstrumentAudioEvent.create(audio704, 0.000000, 1.000000, "SNARE", "B8", 1.000000));
    // Instrument Audio Chords
    audio706 = insert(InstrumentAudio.create(instrument29, "Closed Hat 4", "e84f17a2-34e3-49ee-9c0a-aa71ecef0880-instrument-29-audio.wav", 0.000100, 0.250000, 121.000000, 5333.330000, 0.600000));
    // Instrument Audio Events
    audioEvent707 = insert(InstrumentAudioEvent.create(audio706, 0.000000, 1.000000, "HIHAT", "E8", 1.000000));
    // Instrument Audio Chords
    audio708 = insert(InstrumentAudio.create(instrument29, "Gated Industrial Kick", "ef7b2620-beaa-44b3-a07e-ac0498ab7831-instrument-29-audio.wav", 0.000300, 0.594000, 121.000000, 86.331000, 0.600000));
    // Instrument Audio Events
    audioEvent709 = insert(InstrumentAudioEvent.create(audio708, 0.000000, 1.000000, "KICK", "F2", 1.000000));
    // Instrument Audio Chords
    audio710 = insert(InstrumentAudio.create(instrument29, "Tom 3", "f557e49a-abb3-4ccc-87e2-1595924237fa-instrument-29-audio.wav", 0.000000, 0.375000, 121.000000, 107.383000, 0.600000));
    // Instrument Audio Events
    audioEvent711 = insert(InstrumentAudioEvent.create(audio710, 0.000000, 1.000000, "TOM", "A2", 1.000000));
    // Instrument Audio Chords
    audio712 = insert(InstrumentAudio.create(instrument29, "Clicky Industrial Kick", "2e89ef50-4ded-49b5-9c2c-8c826eb0e49d-instrument-29-audio.wav", 0.000000, 0.391000, 121.000000, 1920.000000, 0.600000));
    // Instrument Audio Events
    audioEvent713 = insert(InstrumentAudioEvent.create(audio712, 0.000000, 1.000000, "KICK", "B6", 1.000000));
    // Instrument Audio Chords
    audio714 = insert(InstrumentAudio.create(instrument29, "Popcorn Snare", "c70e0246-bba7-448c-a45a-3876d9105f8a-instrument-29-audio.wav", 0.000200, 0.375000, 121.000000, 375.000000, 0.600000));
    // Instrument Audio Events
    audioEvent715 = insert(InstrumentAudioEvent.create(audio714, 0.000000, 1.000000, "SNARE", "F#4", 1.000000));
    // Instrument Audio Chords
    audio716 = insert(InstrumentAudio.create(instrument29, "Snappy Snare 2", "086e9930-f691-45d8-a254-aaf3660f4e4a-instrument-29-audio.wav", 0.000000, 0.375000, 121.000000, 214.286000, 0.600000));
    // Instrument Audio Events
    audioEvent717 = insert(InstrumentAudioEvent.create(audio716, 0.000000, 1.000000, "SNARE", "A3", 1.000000));
    // Instrument Audio Chords
    audio718 = insert(InstrumentAudio.create(instrument29, "Punchy Kick", "c9edfab8-eab3-41a3-b58e-eb5d514fb134-instrument-29-audio.wav", 0.000100, 0.156000, 121.000000, 91.429000, 0.600000));
    // Instrument Audio Events
    audioEvent719 = insert(InstrumentAudioEvent.create(audio718, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument Flammy Clap
    instrument25 = insert(Instrument.create(user3, library4, "Percussive", "Published", "Flammy Clap", 0.600000));
    // Instrument Memes
    // Instrument Audios
    audio720 = insert(InstrumentAudio.create(instrument25, "Clap", "d38499b7-cd9c-40b5-b858-69c6b867d614-instrument-25-audio.wav", 0.002000, 0.159000, 121.000000, 595.946000, 0.600000));
    // Instrument Audio Events
    audioEvent721 = insert(InstrumentAudioEvent.create(audio720, 0.000000, 1.000000, "SNARE", "D5", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument New Earth Large
    instrument42 = insert(Instrument.create(user3, library3, "Percussive", "Published", "New Earth Large", 0.600000));
    // Instrument Memes
    // Instrument Audios
    audio722 = insert(InstrumentAudio.create(instrument42, "Dun.Dun.Kick.Short", "00610417-608b-4105-8528-583ed910a0c2-instrument-42-audio.wav", 0.000000, 0.193000, 121.000000, 10666.700000, 0.600000));
    // Instrument Audio Events
    audioEvent723 = insert(InstrumentAudioEvent.create(audio722, 0.000000, 1.000000, "KICK", "E9", 1.000000));
    // Instrument Audio Chords
    audio724 = insert(InstrumentAudio.create(instrument42, "Dun.Dun.Kick.Hype.Short", "9526f54e-54b9-4590-a5d2-60c897d2dd1c-instrument-42-audio.wav", 0.004000, 0.310000, 121.000000, 10666.700000, 0.600000));
    // Instrument Audio Events
    audioEvent725 = insert(InstrumentAudioEvent.create(audio724, 0.000000, 1.000000, "KICK", "E9", 1.000000));
    // Instrument Audio Chords
    audio726 = insert(InstrumentAudio.create(instrument42, "Xylo.Tom.4", "d157a565-4237-4b5e-b54d-0827ea9fa236-instrument-42-audio.wav", 0.000000, 0.067000, 121.000000, 581.818000, 0.600000));
    // Instrument Audio Events
    audioEvent727 = insert(InstrumentAudioEvent.create(audio726, 0.000000, 1.000000, "TOMMID", "D5", 1.000000));
    // Instrument Audio Chords
    audio728 = insert(InstrumentAudio.create(instrument42, "Shekere.Riser", "87c74cb2-ca46-43be-b890-9b1a7a4cc607-instrument-42-audio.wav", 0.000000, 1.996000, 121.000000, 1116.280000, 0.600000));
    // Instrument Audio Events
    audioEvent729 = insert(InstrumentAudioEvent.create(audio728, 0.000000, 4.000000, "CRASH", "C#6", 1.000000));
    // Instrument Audio Chords
    audio730 = insert(InstrumentAudio.create(instrument42, "Brushy.Snare.Short", "6576f0c7-83e0-42ce-97bf-01f5d5194339-instrument-42-audio.wav", 0.000000, 0.152000, 121.000000, 251.309000, 0.600000));
    // Instrument Audio Events
    audioEvent731 = insert(InstrumentAudioEvent.create(audio730, 0.000000, 1.000000, "SNARE", "B3", 1.000000));
    // Instrument Audio Chords
    audio732 = insert(InstrumentAudio.create(instrument42, "Xylo.Tom.2", "9967e4f9-6690-41da-9cff-589f984be34d-instrument-42-audio.wav", 0.000000, 0.083000, 121.000000, 627.451000, 0.600000));
    // Instrument Audio Events
    audioEvent733 = insert(InstrumentAudioEvent.create(audio732, 0.000000, 1.000000, "TOMHI", "Eb5", 1.000000));
    // Instrument Audio Chords
    audio734 = insert(InstrumentAudio.create(instrument42, "Dun.Dun.Tom", "561a97ee-cde4-482e-9c1e-589e09ababb9-instrument-42-audio.wav", 0.000000, 0.174000, 121.000000, 124.352000, 0.600000));
    // Instrument Audio Events
    audioEvent735 = insert(InstrumentAudioEvent.create(audio734, 0.000000, 1.000000, "TOMLOW", "B2", 1.000000));
    // Instrument Audio Chords
    audio736 = insert(InstrumentAudio.create(instrument42, "Flam.Clap.3", "3acc0db1-f258-4698-99cb-ece7851231c9-instrument-42-audio.wav", 0.000000, 0.238000, 121.000000, 101.480000, 0.600000));
    // Instrument Audio Events
    audioEvent737 = insert(InstrumentAudioEvent.create(audio736, 0.000000, 1.000000, "SNARE", "Ab2", 1.000000));
    // Instrument Audio Chords
    audio738 = insert(InstrumentAudio.create(instrument42, "Dun.Dun.Tom.2", "45909cf6-829d-4860-9299-f5194eaeebf5-instrument-42-audio.wav", 0.000000, 0.754000, 121.000000, 183.908000, 0.600000));
    // Instrument Audio Events
    audioEvent739 = insert(InstrumentAudioEvent.create(audio738, 0.000000, 1.000000, "TOMMID", "F#3", 1.000000));
    // Instrument Audio Chords
    audio740 = insert(InstrumentAudio.create(instrument42, "Snare.w/Tambo.Tail", "8c643b60-a107-4b88-9fac-554105112236-instrument-42-audio.wav", 0.000000, 0.325000, 121.000000, 5647.060000, 0.600000));
    // Instrument Audio Events
    audioEvent741 = insert(InstrumentAudioEvent.create(audio740, 0.000000, 1.000000, "SNARE", "F8", 1.000000));
    audioEvent742 = insert(InstrumentAudioEvent.create(audio740, 0.000000, 1.000000, "SNARE", "F8", 1.000000));
    // Instrument Audio Chords
    audio743 = insert(InstrumentAudio.create(instrument42, "Dun.Dun.Kick.Hype", "5b75b290-4eb2-4fc9-8e8b-fbf618aec882-instrument-42-audio.wav", 0.000000, 0.310000, 121.000000, 10666.700000, 0.600000));
    // Instrument Audio Events
    audioEvent744 = insert(InstrumentAudioEvent.create(audio743, 0.000000, 1.000000, "KICK", "E9", 1.000000));
    // Instrument Audio Chords
    audio745 = insert(InstrumentAudio.create(instrument42, "Shekere.1", "8fdee415-877a-4920-94e9-49cf501ceaae-instrument-42-audio.wav", 0.000000, 0.576000, 121.000000, 2742.860000, 0.600000));
    // Instrument Audio Events
    audioEvent746 = insert(InstrumentAudioEvent.create(audio745, 0.000000, 1.000000, "HIHATOPEN", "F7", 1.000000));
    // Instrument Audio Chords
    audio747 = insert(InstrumentAudio.create(instrument42, "Brushy.Closed.Hat", "548f6c29-b96f-41cb-93be-512ede500ac8-instrument-42-audio.wav", 0.000000, 0.286000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent748 = insert(InstrumentAudioEvent.create(audio747, 0.000000, 1.000000, "HIHATCLOSED", "B8", 1.000000));
    // Instrument Audio Chords
    audio749 = insert(InstrumentAudio.create(instrument42, "Xylo.Tom.1", "60585413-bdea-4303-90c0-479ae7ad2eaa-instrument-42-audio.wav", 0.000000, 0.089000, 121.000000, 780.488000, 0.600000));
    // Instrument Audio Events
    audioEvent750 = insert(InstrumentAudioEvent.create(audio749, 0.000000, 1.000000, "TOMHI", "G5", 1.000000));
    // Instrument Audio Chords
    audio751 = insert(InstrumentAudio.create(instrument42, "Flam.Clap.1", "5cfa8003-1ee8-4d4f-81e1-5c51f5cbda65-instrument-42-audio.wav", 0.000000, 0.101000, 121.000000, 979.592000, 0.600000));
    // Instrument Audio Events
    audioEvent752 = insert(InstrumentAudioEvent.create(audio751, 0.000000, 1.000000, "SNARE", "B5", 1.000000));
    // Instrument Audio Chords
    audio753 = insert(InstrumentAudio.create(instrument42, "Brushy.Snare", "f50a9ce5-ab94-421e-823e-c4e6b911dd74-instrument-42-audio.wav", 0.000000, 0.501000, 121.000000, 251.309000, 0.600000));
    // Instrument Audio Events
    audioEvent754 = insert(InstrumentAudioEvent.create(audio753, 0.000000, 1.000000, "SNARE", "B3", 1.000000));
    // Instrument Audio Chords
    audio755 = insert(InstrumentAudio.create(instrument42, "Dun.Dun.Kick", "6d86fdf8-1c65-41a7-b3fa-571bf05451e3-instrument-42-audio.wav", 0.011000, 0.496000, 121.000000, 10666.700000, 0.600000));
    // Instrument Audio Events
    audioEvent756 = insert(InstrumentAudioEvent.create(audio755, 0.000000, 1.000000, "KICK", "E9", 1.000000));
    // Instrument Audio Chords
    audio757 = insert(InstrumentAudio.create(instrument42, "Shekere.4", "ca7878ac-d25b-480f-9abc-286f5eafa867-instrument-42-audio.wav", 0.000000, 0.271000, 121.000000, 2400.000000, 0.600000));
    // Instrument Audio Events
    audioEvent758 = insert(InstrumentAudioEvent.create(audio757, 0.000000, 1.000000, "HIHATCLOSED", "D7", 1.000000));
    // Instrument Audio Chords
    audio759 = insert(InstrumentAudio.create(instrument42, "Snare.w/Tambo.Tail.Short", "db928de2-0d88-4bc6-89cf-91e72366d59f-instrument-42-audio.wav", 0.000000, 0.155000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent760 = insert(InstrumentAudioEvent.create(audio759, 0.000000, 1.000000, "SNARE", "F#8", 1.000000));
    // Instrument Audio Chords
    audio761 = insert(InstrumentAudio.create(instrument42, "Xylo.Tom.6", "38910daa-0fa4-4c10-a49d-53560df19b76-instrument-42-audio.wav", 0.000000, 0.112000, 121.000000, 304.762000, 0.600000));
    // Instrument Audio Events
    audioEvent762 = insert(InstrumentAudioEvent.create(audio761, 0.000000, 1.000000, "TOMMID", "D#4", 1.000000));
    // Instrument Audio Chords
    audio763 = insert(InstrumentAudio.create(instrument42, "Shekere.2", "cd1b24a3-2261-4964-b7c4-93ebc7930127-instrument-42-audio.wav", 0.000000, 0.327000, 121.000000, 2461.540000, 0.600000));
    // Instrument Audio Events
    audioEvent764 = insert(InstrumentAudioEvent.create(audio763, 0.000000, 1.000000, "HIHATCLOSED", "Eb7", 1.000000));
    // Instrument Audio Chords
    audio765 = insert(InstrumentAudio.create(instrument42, "Xylo.Tom.3", "63eabc53-9273-468e-8f9a-8396ef172767-instrument-42-audio.wav", 0.000000, 0.083000, 121.000000, 627.451000, 0.600000));
    // Instrument Audio Events
    audioEvent766 = insert(InstrumentAudioEvent.create(audio765, 0.000000, 1.000000, "TOMHI", "Eb5", 1.000000));
    // Instrument Audio Chords
    audio767 = insert(InstrumentAudio.create(instrument42, "Flam.Clap.2", "67d001b9-7279-4dc0-9800-26b1e086c16a-instrument-42-audio.wav", 0.000000, 0.088000, 121.000000, 1333.330000, 0.600000));
    // Instrument Audio Events
    audioEvent768 = insert(InstrumentAudioEvent.create(audio767, 0.000000, 1.000000, "SNARE", "E6", 1.000000));
    // Instrument Audio Chords
    audio769 = insert(InstrumentAudio.create(instrument42, "Dun.Dun.Kick.Sub", "592b444b-0137-45ed-bf92-c9269f16aad9-instrument-42-audio.wav", 0.000000, 0.699000, 121.000000, 71.588000, 0.600000));
    // Instrument Audio Events
    audioEvent770 = insert(InstrumentAudioEvent.create(audio769, 0.000000, 1.000000, "KICK", "D2", 1.000000));
    // Instrument Audio Chords
    audio771 = insert(InstrumentAudio.create(instrument42, "Brushy.Open.Hat", "5ff42f3d-24cf-405c-9fa8-023198ecab2b-instrument-42-audio.wav", 0.000000, 0.295000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent772 = insert(InstrumentAudioEvent.create(audio771, 0.000000, 1.000000, "HIHATOPEN", "D8", 1.000000));
    // Instrument Audio Chords
    audio773 = insert(InstrumentAudio.create(instrument42, "Shekere.3", "d400fe01-c7ce-4bf2-83f6-70034e77fa83-instrument-42-audio.wav", 0.000000, 0.238000, 121.000000, 2461.540000, 0.600000));
    // Instrument Audio Events
    audioEvent774 = insert(InstrumentAudioEvent.create(audio773, 0.000000, 1.000000, "HIHATCLOSED", "Eb7", 1.000000));
    // Instrument Audio Chords
    audio775 = insert(InstrumentAudio.create(instrument42, "Xylo.Tom.5", "a91858d9-8306-4bb9-9285-3c53828738ff-instrument-42-audio.wav", 0.000000, 0.101000, 121.000000, 415.584000, 0.600000));
    // Instrument Audio Events
    audioEvent776 = insert(InstrumentAudioEvent.create(audio775, 0.000000, 1.000000, "TOMMID", "Ab4", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument Pots & Pans
    instrument5 = insert(Instrument.create(user1, library1, "Percussive", "Published", "Pots & Pans", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument5, "Cool"));
    insert(InstrumentMeme.create(instrument5, "Hot"));
    insert(InstrumentMeme.create(instrument5, "Classic"));
    insert(InstrumentMeme.create(instrument5, "Deep"));
    insert(InstrumentMeme.create(instrument5, "Hard"));
    // Instrument Audios
    audio777 = insert(InstrumentAudio.create(instrument5, "Conga M_1", "983fc7a1-a1ef-466f-be44-cc1e227ae449-instrument-5-audio.wav", 0.000000, 0.318000, 120.000000, 565.385000, 0.600000));
    // Instrument Audio Events
    audioEvent778 = insert(InstrumentAudioEvent.create(audio777, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio779 = insert(InstrumentAudio.create(instrument5, "Tom L_7", "b51678cb-50a0-4994-980a-62bf126ca445-instrument-5-audio.wav", 0.001000, 0.674000, 120.000000, 531.325000, 0.600000));
    // Instrument Audio Events
    audioEvent780 = insert(InstrumentAudioEvent.create(audio779, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio781 = insert(InstrumentAudio.create(instrument5, "Snare Q_4", "8e17510c-a877-42a6-addc-95ef7d559757-instrument-5-audio.wav", 0.001000, 1.257000, 120.000000, 5512.500000, 0.600000));
    // Instrument Audio Events
    audioEvent782 = insert(InstrumentAudioEvent.create(audio781, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio783 = insert(InstrumentAudio.create(instrument5, "Vocal What 3", "489c5976-cbda-4449-a8cf-67d653b77dbf-instrument-5-audio.wav", 0.040000, 0.407000, 120.000000, 370.588000, 0.600000));
    // Instrument Audio Events
    audioEvent784 = insert(InstrumentAudioEvent.create(audio783, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio785 = insert(InstrumentAudio.create(instrument5, "Tom H_3", "8494ac91-a1ef-4045-9f1f-3a1b4a53ee3d-instrument-5-audio.wav", 0.000000, 2.346000, 120.000000, 1378.120000, 0.600000));
    // Instrument Audio Events
    audioEvent786 = insert(InstrumentAudioEvent.create(audio785, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio787 = insert(InstrumentAudio.create(instrument5, "Tom L_1", "568d1c74-a43e-44fc-ab53-0d1d701f6f0f-instrument-5-audio.wav", 0.000000, 0.851000, 120.000000, 364.463000, 0.600000));
    // Instrument Audio Events
    audioEvent788 = insert(InstrumentAudioEvent.create(audio787, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio789 = insert(InstrumentAudio.create(instrument5, "Conga M_9", "2d2d76f7-9d76-41c6-9e55-0b94703d487c-instrument-5-audio.wav", 0.000000, 0.407000, 120.000000, 531.325000, 0.600000));
    // Instrument Audio Events
    audioEvent790 = insert(InstrumentAudioEvent.create(audio789, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio791 = insert(InstrumentAudio.create(instrument5, "Kick 12_339", "ccfc6b74-c939-481f-b59d-caced86b2528-instrument-5-audio.wav", 0.000000, 0.457000, 120.000000, 3675.000000, 0.600000));
    // Instrument Audio Events
    audioEvent792 = insert(InstrumentAudioEvent.create(audio791, 0.000000, 1.000000, "KICK", "x", 2.000000));
    // Instrument Audio Chords
    audio793 = insert(InstrumentAudio.create(instrument5, "Snare Q_7", "7fd96254-d9cf-4ad6-9899-dee564543853-instrument-5-audio.wav", 0.001000, 0.653000, 120.000000, 5512.500000, 0.600000));
    // Instrument Audio Events
    audioEvent794 = insert(InstrumentAudioEvent.create(audio793, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio795 = insert(InstrumentAudio.create(instrument5, "Hihat Closed A_9", "96df8da4-5be9-4a0f-a97b-5f8c0d28f161-instrument-5-audio.wav", 0.000000, 0.432000, 120.000000, 1454.550000, 0.600000));
    // Instrument Audio Events
    audioEvent796 = insert(InstrumentAudioEvent.create(audio795, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio797 = insert(InstrumentAudio.create(instrument5, "Tom H_5", "c18a2f87-df5f-421a-aa59-89fda817210c-instrument-5-audio.wav", 0.000000, 3.133000, 120.000000, 189.270000, 0.600000));
    // Instrument Audio Events
    audioEvent798 = insert(InstrumentAudioEvent.create(audio797, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio799 = insert(InstrumentAudio.create(instrument5, "Snare Q_8", "725e8281-c845-4a87-9a37-9117b1e6a830-instrument-5-audio.wav", 0.002000, 0.799000, 120.000000, 355.645000, 0.600000));
    // Instrument Audio Events
    audioEvent800 = insert(InstrumentAudioEvent.create(audio799, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio801 = insert(InstrumentAudio.create(instrument5, "Tom L_2", "6ffdef87-909f-4b67-a2f7-fadbb3a76e33-instrument-5-audio.wav", 0.000000, 0.528000, 120.000000, 257.895000, 0.600000));
    // Instrument Audio Events
    audioEvent802 = insert(InstrumentAudioEvent.create(audio801, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio803 = insert(InstrumentAudio.create(instrument5, "Snare Q_6", "83fbed4b-648c-4886-9079-f220fb0dc9fb-instrument-5-audio.wav", 0.001000, 0.659000, 120.000000, 134.451000, 0.600000));
    // Instrument Audio Events
    audioEvent804 = insert(InstrumentAudioEvent.create(audio803, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio805 = insert(InstrumentAudio.create(instrument5, "Vocal What 2", "70d22a2a-a888-460f-9dfa-01bae076adfe-instrument-5-audio.wav", 0.027000, 0.276000, 120.000000, 416.038000, 0.600000));
    // Instrument Audio Events
    audioEvent806 = insert(InstrumentAudioEvent.create(audio805, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio807 = insert(InstrumentAudio.create(instrument5, "Tom L_9", "50f516a9-faaa-4091-848d-651d96ecc7be-instrument-5-audio.wav", 0.000000, 0.751000, 120.000000, 176.400000, 0.600000));
    // Instrument Audio Events
    audioEvent808 = insert(InstrumentAudioEvent.create(audio807, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio809 = insert(InstrumentAudio.create(instrument5, "Conga M_7", "02dde877-01b4-432d-8d22-f1458917154b-instrument-5-audio.wav", 0.001000, 0.502000, 120.000000, 420.000000, 0.600000));
    // Instrument Audio Events
    audioEvent810 = insert(InstrumentAudioEvent.create(audio809, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio811 = insert(InstrumentAudio.create(instrument5, "Vocal Woah", "7ac9d00c-0b24-49ad-8cbb-c586ac0f080f-instrument-5-audio.wav", 0.020000, 0.488000, 120.000000, 604.110000, 0.600000));
    // Instrument Audio Events
    audioEvent812 = insert(InstrumentAudioEvent.create(audio811, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio813 = insert(InstrumentAudio.create(instrument5, "Tom L_5", "f6f79c74-f1e0-459b-9728-46f59bd14ee7-instrument-5-audio.wav", 0.001000, 0.608000, 120.000000, 428.155000, 0.600000));
    // Instrument Audio Events
    audioEvent814 = insert(InstrumentAudioEvent.create(audio813, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio815 = insert(InstrumentAudio.create(instrument5, "Hihat Closed A_6", "e173c291-60d6-4f9a-a422-d2d8c99bd9b3-instrument-5-audio.wav", 0.003000, 0.425000, 120.000000, 3428.570000, 0.600000));
    // Instrument Audio Events
    audioEvent816 = insert(InstrumentAudioEvent.create(audio815, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio817 = insert(InstrumentAudio.create(instrument5, "Snare Q_10", "b14d6a26-1e35-4f7c-bbfb-6fd262c2d35f-instrument-5-audio.wav", 0.000000, 1.631000, 120.000000, 1378.120000, 0.600000));
    // Instrument Audio Events
    audioEvent818 = insert(InstrumentAudioEvent.create(audio817, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio819 = insert(InstrumentAudio.create(instrument5, "Tom H_2", "2c2d8ba8-911b-4480-a774-c37102c12e90-instrument-5-audio.wav", 0.009000, 2.036000, 120.000000, 1378.120000, 0.600000));
    // Instrument Audio Events
    audioEvent820 = insert(InstrumentAudioEvent.create(audio819, 0.000000, 1.000000, "KICK", "X", 1.000000));
    // Instrument Audio Chords
    audio821 = insert(InstrumentAudio.create(instrument5, "Tom L_10", "38c92218-882d-4714-a493-14261e07c4fa-instrument-5-audio.wav", 0.000000, 0.741000, 120.000000, 302.055000, 0.600000));
    // Instrument Audio Events
    audioEvent822 = insert(InstrumentAudioEvent.create(audio821, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio823 = insert(InstrumentAudio.create(instrument5, "Tom L_8", "f1bac880-fede-4c5d-9249-956f5e179d62-instrument-5-audio.wav", 0.000000, 0.835000, 120.000000, 290.132000, 0.600000));
    // Instrument Audio Events
    audioEvent824 = insert(InstrumentAudioEvent.create(audio823, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio825 = insert(InstrumentAudio.create(instrument5, "Hihat Closed A_3", "86d61872-a9bf-4b68-b4df-397be09bfe5c-instrument-5-audio.wav", 0.007000, 1.051000, 120.000000, 3428.570000, 0.600000));
    // Instrument Audio Events
    audioEvent826 = insert(InstrumentAudioEvent.create(audio825, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio827 = insert(InstrumentAudio.create(instrument5, "Snare Q_1", "21369f18-b2b6-4d8b-bd28-de36f294b67e-instrument-5-audio.wav", 0.000000, 1.206000, 120.000000, 5512.500000, 0.600000));
    // Instrument Audio Events
    audioEvent828 = insert(InstrumentAudioEvent.create(audio827, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio829 = insert(InstrumentAudio.create(instrument5, "Tom H_6", "91f5c7de-609d-48fd-a527-c7b132ee2af5-instrument-5-audio.wav", 0.000000, 2.984000, 120.000000, 1336.360000, 0.600000));
    // Instrument Audio Events
    audioEvent830 = insert(InstrumentAudioEvent.create(audio829, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio831 = insert(InstrumentAudio.create(instrument5, "Snare Q_9", "0818bf78-3838-43a5-8665-7f8f2814bfc4-instrument-5-audio.wav", 0.003000, 0.583000, 120.000000, 249.153000, 0.600000));
    // Instrument Audio Events
    audioEvent832 = insert(InstrumentAudioEvent.create(audio831, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio833 = insert(InstrumentAudio.create(instrument5, "Tom L_6", "01e988c0-3821-4ba2-8223-70643f3c27cf-instrument-5-audio.wav", 0.000000, 0.736000, 120.000000, 408.333000, 0.600000));
    // Instrument Audio Events
    audioEvent834 = insert(InstrumentAudioEvent.create(audio833, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio835 = insert(InstrumentAudio.create(instrument5, "Vocal What 1", "cccc3d64-9cb9-468d-be42-e1ec29ba65b1-instrument-5-audio.wav", 0.058000, 0.401000, 120.000000, 390.265000, 0.600000));
    // Instrument Audio Events
    audioEvent836 = insert(InstrumentAudioEvent.create(audio835, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio837 = insert(InstrumentAudio.create(instrument5, "Tom H_1", "2f4bf7a2-744e-47cc-b5c2-da0a846cab91-instrument-5-audio.wav", 0.000000, 1.008000, 120.000000, 1422.580000, 0.600000));
    // Instrument Audio Events
    audioEvent838 = insert(InstrumentAudioEvent.create(audio837, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio839 = insert(InstrumentAudio.create(instrument5, "Hihat Closed A_7", "de082694-4a02-48a4-92d1-83c2d2b7dd92-instrument-5-audio.wav", 0.001000, 0.600000, 120.000000, 1263.160000, 0.600000));
    // Instrument Audio Events
    audioEvent840 = insert(InstrumentAudioEvent.create(audio839, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio841 = insert(InstrumentAudio.create(instrument5, "Snare Q_2", "23d5847f-56e6-4b79-99ad-6dfd13b9c5b3-instrument-5-audio.wav", 0.001000, 1.008000, 120.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent842 = insert(InstrumentAudioEvent.create(audio841, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio843 = insert(InstrumentAudio.create(instrument5, "Kick 28_339", "cd42b8a7-c820-43a2-beb1-f5fec4634050-instrument-5-audio.wav", 0.024000, 0.653000, 120.000000, 980.000000, 0.600000));
    // Instrument Audio Events
    audioEvent844 = insert(InstrumentAudioEvent.create(audio843, 0.000000, 1.000000, "KICK", "x", 2.000000));
    // Instrument Audio Chords
    audio845 = insert(InstrumentAudio.create(instrument5, "Hihat Open F_6", "ed5b3f4c-a6e3-424b-b8ba-34c317640903-instrument-5-audio.wav", 0.000000, 1.348000, 120.000000, 432.353000, 0.600000));
    // Instrument Audio Events
    audioEvent846 = insert(InstrumentAudioEvent.create(audio845, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio847 = insert(InstrumentAudio.create(instrument5, "Hihat Closed A_5", "8a536dae-3727-488f-8895-a0b047620a38-instrument-5-audio.wav", 0.001000, 0.537000, 120.000000, 888.889000, 0.600000));
    // Instrument Audio Events
    audioEvent848 = insert(InstrumentAudioEvent.create(audio847, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio849 = insert(InstrumentAudio.create(instrument5, "Hihat Open F_2", "13db8e43-4266-444a-9edd-c5a5cb2442b4-instrument-5-audio.wav", 0.000000, 1.506000, 120.000000, 182.988000, 0.600000));
    // Instrument Audio Events
    audioEvent850 = insert(InstrumentAudioEvent.create(audio849, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio851 = insert(InstrumentAudio.create(instrument5, "Conga M_1", "faf2e9c6-6b12-445e-9b2c-93966451ff5e-instrument-5-audio.wav", 0.000000, 0.318000, 120.000000, 565.385000, 0.600000));
    // Instrument Audio Events
    audioEvent852 = insert(InstrumentAudioEvent.create(audio851, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio853 = insert(InstrumentAudio.create(instrument5, "Snare Q_5", "62536d52-8600-4941-ac04-a72106079610-instrument-5-audio.wav", 0.002000, 0.405000, 120.000000, 1025.580000, 0.600000));
    // Instrument Audio Events
    audioEvent854 = insert(InstrumentAudioEvent.create(audio853, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio855 = insert(InstrumentAudio.create(instrument5, "Vocal Watch Me", "649a2969-6b98-4201-89fc-968d6414f578-instrument-5-audio.wav", 0.050000, 0.807000, 120.000000, 1225.000000, 0.600000));
    // Instrument Audio Events
    audioEvent856 = insert(InstrumentAudioEvent.create(audio855, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio857 = insert(InstrumentAudio.create(instrument5, "Hihat Open F_3", "3f0dbe3a-d11a-4e9f-a642-befe5747dd01-instrument-5-audio.wav", 0.000000, 2.567000, 120.000000, 183.750000, 0.600000));
    // Instrument Audio Events
    audioEvent858 = insert(InstrumentAudioEvent.create(audio857, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio859 = insert(InstrumentAudio.create(instrument5, "Hihat Open F_4", "57ff6b97-fedb-4e3f-b963-840ba8fd101b-instrument-5-audio.wav", 0.035000, 2.617000, 120.000000, 416.038000, 0.600000));
    // Instrument Audio Events
    audioEvent860 = insert(InstrumentAudioEvent.create(audio859, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio861 = insert(InstrumentAudio.create(instrument5, "Snare Q_3", "a448d6b9-4669-4f17-883a-8dd8c5ce0b8e-instrument-5-audio.wav", 0.000000, 0.915000, 120.000000, 5512.500000, 0.600000));
    // Instrument Audio Events
    audioEvent862 = insert(InstrumentAudioEvent.create(audio861, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio863 = insert(InstrumentAudio.create(instrument5, "Tom L_4", "2b9af025-2616-4d03-890f-b74df3413abe-instrument-5-audio.wav", 0.000000, 0.592000, 120.000000, 11025.000000, 0.600000));
    // Instrument Audio Events
    audioEvent864 = insert(InstrumentAudioEvent.create(audio863, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio865 = insert(InstrumentAudio.create(instrument5, "Hihat Open F_5", "70c7404e-1f17-4a32-8f4a-ff28e7d5797c-instrument-5-audio.wav", 0.000000, 2.734000, 120.000000, 420.000000, 0.600000));
    // Instrument Audio Events
    audioEvent866 = insert(InstrumentAudioEvent.create(audio865, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio867 = insert(InstrumentAudio.create(instrument5, "Conga M_6", "3bdc44e7-e464-4a0f-a080-ab3d529ac9dc-instrument-5-audio.wav", 0.001000, 0.512000, 120.000000, 612.500000, 0.600000));
    // Instrument Audio Events
    audioEvent868 = insert(InstrumentAudioEvent.create(audio867, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio869 = insert(InstrumentAudio.create(instrument5, "Hihat Open F_7", "8d7c72dc-92bb-4ffa-82ff-13750c8ddbfc-instrument-5-audio.wav", 0.000000, 2.264000, 120.000000, 183.750000, 0.600000));
    // Instrument Audio Events
    audioEvent870 = insert(InstrumentAudioEvent.create(audio869, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio871 = insert(InstrumentAudio.create(instrument5, "Conga M_4", "f6e912f5-d582-4044-b73b-6e004bb32a15-instrument-5-audio.wav", 0.000000, 0.600000, 120.000000, 612.500000, 0.600000));
    // Instrument Audio Events
    audioEvent872 = insert(InstrumentAudioEvent.create(audio871, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio873 = insert(InstrumentAudio.create(instrument5, "Hihat Open F_8", "7eae03f7-d1aa-42e2-a928-ff6f7b00b25d-instrument-5-audio.wav", 0.000000, 2.595000, 120.000000, 182.988000, 0.600000));
    // Instrument Audio Events
    audioEvent874 = insert(InstrumentAudioEvent.create(audio873, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio875 = insert(InstrumentAudio.create(instrument5, "Hihat Closed A_10", "e4a06acb-c375-4e9b-a5ce-153b815fe6cb-instrument-5-audio.wav", 0.002000, 0.307000, 120.000000, 3000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent876 = insert(InstrumentAudioEvent.create(audio875, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio877 = insert(InstrumentAudio.create(instrument5, "Tom L_3", "dd32a686-ef3a-43c4-a3e1-13353d067026-instrument-5-audio.wav", 0.000000, 0.624000, 120.000000, 110.526000, 0.600000));
    // Instrument Audio Events
    audioEvent878 = insert(InstrumentAudioEvent.create(audio877, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio879 = insert(InstrumentAudio.create(instrument5, "Tom H_8", "0e5c97c1-ad2a-4cb5-a1f5-10224c7cec3c-instrument-5-audio.wav", 0.000000, 2.698000, 120.000000, 1633.330000, 0.600000));
    // Instrument Audio Events
    audioEvent880 = insert(InstrumentAudioEvent.create(audio879, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio881 = insert(InstrumentAudio.create(instrument5, "Conga M_3", "c8d1affb-9b7c-4661-bf31-cd80dc2a9ce1-instrument-5-audio.wav", 0.000000, 0.602000, 120.000000, 588.000000, 0.600000));
    // Instrument Audio Events
    audioEvent882 = insert(InstrumentAudioEvent.create(audio881, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio883 = insert(InstrumentAudio.create(instrument5, "Conga M_3", "710b3011-cb1e-4065-a514-1e6e4fd19bec-instrument-5-audio.wav", 0.000000, 0.427000, 120.000000, 612.500000, 0.600000));
    // Instrument Audio Events
    audioEvent884 = insert(InstrumentAudioEvent.create(audio883, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio885 = insert(InstrumentAudio.create(instrument5, "Vocal Hoh", "5709e633-bd69-407b-b6ba-420395b221de-instrument-5-audio.wav", 0.028000, 0.476000, 120.000000, 689.062000, 0.600000));
    // Instrument Audio Events
    audioEvent886 = insert(InstrumentAudioEvent.create(audio885, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio887 = insert(InstrumentAudio.create(instrument5, "Snare Q_11", "88ba75c5-9727-43a3-9ef0-856abe729f78-instrument-5-audio.wav", 0.000000, 1.524000, 120.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent888 = insert(InstrumentAudioEvent.create(audio887, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio889 = insert(InstrumentAudio.create(instrument5, "Hihat Closed A_4", "92f61e58-7225-48bb-91f3-b71fcf7aef5a-instrument-5-audio.wav", 0.000000, 0.623000, 120.000000, 888.889000, 0.600000));
    // Instrument Audio Events
    audioEvent890 = insert(InstrumentAudioEvent.create(audio889, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio891 = insert(InstrumentAudio.create(instrument5, "Kick 24_339", "a0d1938b-9f3d-47b3-a98f-fb0a429e6df7-instrument-5-audio.wav", 0.013000, 0.547000, 120.000000, 88.024000, 0.600000));
    // Instrument Audio Events
    audioEvent892 = insert(InstrumentAudioEvent.create(audio891, 0.000000, 1.000000, "KICK", "x", 2.000000));
    // Instrument Audio Chords
    audio893 = insert(InstrumentAudio.create(instrument5, "Vocal Oobah", "a7779c99-55b0-4067-819d-a8203a157cd6-instrument-5-audio.wav", 0.000000, 0.904000, 120.000000, 397.297000, 0.600000));
    // Instrument Audio Events
    audioEvent894 = insert(InstrumentAudioEvent.create(audio893, 0.020000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio895 = insert(InstrumentAudio.create(instrument5, "Tom H_7", "2a525acb-dc9a-47f4-b105-89dc3332d78b-instrument-5-audio.wav", 0.000000, 1.738000, 120.000000, 1764.000000, 0.600000));
    // Instrument Audio Events
    audioEvent896 = insert(InstrumentAudioEvent.create(audio895, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio897 = insert(InstrumentAudio.create(instrument5, "Vocal Play It", "53fc9c8c-2412-4133-b088-9bac349e6794-instrument-5-audio.wav", 0.064000, 0.358000, 120.000000, 116.053000, 0.600000));
    // Instrument Audio Events
    audioEvent898 = insert(InstrumentAudioEvent.create(audio897, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio899 = insert(InstrumentAudio.create(instrument5, "Conga M_5", "0e47652d-265b-4c83-8c4f-c14a34fc9689-instrument-5-audio.wav", 0.000000, 0.466000, 120.000000, 612.500000, 0.600000));
    // Instrument Audio Events
    audioEvent900 = insert(InstrumentAudioEvent.create(audio899, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio901 = insert(InstrumentAudio.create(instrument5, "Hihat Open F_1", "4eb40925-8e37-4801-ba2e-cce991c97093-instrument-5-audio.wav", 0.000000, 0.969000, 120.000000, 428.155000, 0.600000));
    // Instrument Audio Events
    audioEvent902 = insert(InstrumentAudioEvent.create(audio901, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio903 = insert(InstrumentAudio.create(instrument5, "Tom H_4", "ee21d28c-6102-4ad7-96a5-49cf5ccaf266-instrument-5-audio.wav", 0.000000, 2.815000, 120.000000, 186.076000, 0.600000));
    // Instrument Audio Events
    audioEvent904 = insert(InstrumentAudioEvent.create(audio903, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio905 = insert(InstrumentAudio.create(instrument5, "Conga M_8", "1c5f4752-e790-47a0-b0d9-4eedd54b24a5-instrument-5-audio.wav", 0.000000, 0.407000, 120.000000, 531.325000, 0.600000));
    // Instrument Audio Events
    audioEvent906 = insert(InstrumentAudioEvent.create(audio905, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio907 = insert(InstrumentAudio.create(instrument5, "Hihat Closed A_8", "7cbe09b2-5fe6-4d7a-b5fa-2f85624e91f5-instrument-5-audio.wav", 0.000000, 0.730000, 120.000000, 1200.000000, 0.600000));
    // Instrument Audio Events
    audioEvent908 = insert(InstrumentAudioEvent.create(audio907, 0.000000, 1.000000, "HIHAT", "X", 1.000000));
    // Instrument Audio Chords
    audio909 = insert(InstrumentAudio.create(instrument5, "Kick 11_339", "dfe7c338-dd80-42ee-94da-19bc53489ca7-instrument-5-audio.wav", 0.000000, 0.569000, 120.000000, 69.014000, 0.600000));
    // Instrument Audio Events
    audioEvent910 = insert(InstrumentAudioEvent.create(audio909, 0.000000, 1.000000, "KICK", "x", 2.000000));
    // Instrument Audio Chords

  }

  private void go7() throws Exception {
    // Insert Percussive-type Instrument Water B (legacy)
    instrument36 = insert(Instrument.create(user1, library1, "Percussive", "Published", "Water B (legacy)", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument36, "Water"));
    // Instrument Audios
    audio911 = insert(InstrumentAudio.create(instrument36, "Kick 0", "c353888f-1d25-4b13-ac8e-fec4cdcd10f3-instrument-36-audio.wav", 0.000000, 0.100000, 121.000000, 153.659000, 0.600000));
    // Instrument Audio Events
    audioEvent912 = insert(InstrumentAudioEvent.create(audio911, 0.000000, 1.000000, "KICK", "Eb3", 1.000000));
    // Instrument Audio Chords
    audio913 = insert(InstrumentAudio.create(instrument36, "Hihat Open 1 Half Edge", "b4195987-56ed-46c6-836d-aa3c05102a80-instrument-36-audio.wav", 0.010000, 0.590000, 121.000000, 518.824000, 0.600000));
    // Instrument Audio Events
    audioEvent914 = insert(InstrumentAudioEvent.create(audio913, 0.000000, 0.500000, "HIHATOPEN", "c5", 1.000000));
    // Instrument Audio Chords
    audio915 = insert(InstrumentAudio.create(instrument36, "Snare 34", "c935b049-2de0-4caf-897d-6cf21763f41c-instrument-36-audio.wav", 0.000000, 1.372000, 121.000000, 172.266000, 0.600000));
    // Instrument Audio Events
    audioEvent916 = insert(InstrumentAudioEvent.create(audio915, 0.000000, 1.000000, "SNARE", "f3", 1.000000));
    // Instrument Audio Chords
    audio917 = insert(InstrumentAudio.create(instrument36, "Hihat Open 2 Half Edge", "ad96a177-2b26-46f6-9aaf-f95a5004480e-instrument-36-audio.wav", 0.001000, 0.455000, 121.000000, 7350.000000, 0.600000));
    // Instrument Audio Events
    audioEvent918 = insert(InstrumentAudioEvent.create(audio917, 0.000000, 0.500000, "HIHATOPEN", "Bb8", 1.000000));
    // Instrument Audio Chords
    audio919 = insert(InstrumentAudio.create(instrument36, "Hihat Closed 1 Pedal", "af2a8ec3-23b8-49b7-8f03-861ca9edf048-instrument-36-audio.wav", 0.020000, 0.130000, 121.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent920 = insert(InstrumentAudioEvent.create(audio919, 0.000000, 0.500000, "HIHAT", "g8", 1.000000));
    // Instrument Audio Chords
    audio921 = insert(InstrumentAudio.create(instrument36, "Tom B2", "83a41117-77b2-406c-824b-88c293e35724-instrument-36-audio.wav", 0.000000, 1.550000, 121.000000, 136.533000, 0.600000));
    // Instrument Audio Events
    audioEvent922 = insert(InstrumentAudioEvent.create(audio921, 0.000000, 1.000000, "TOM", "Db3", 1.000000));
    // Instrument Audio Chords
    audio923 = insert(InstrumentAudio.create(instrument36, "Snare 33", "82f11927-1d2d-4bb9-9636-cd79c5344232-instrument-36-audio.wav", 0.000000, 1.289000, 121.000000, 240.984000, 0.600000));
    // Instrument Audio Events
    audioEvent924 = insert(InstrumentAudioEvent.create(audio923, 0.000000, 1.000000, "SNARE", "B", 1.000000));
    // Instrument Audio Chords
    audio925 = insert(InstrumentAudio.create(instrument36, "Hihat Open 1 Half", "bc144296-7bc8-4993-addd-8bf88381ce49-instrument-36-audio.wav", 0.001000, 0.457000, 121.000000, 331.579000, 0.600000));
    // Instrument Audio Events
    audioEvent926 = insert(InstrumentAudioEvent.create(audio925, 0.000000, 0.500000, "HIHATOPEN", "e4", 1.000000));
    // Instrument Audio Chords
    audio927 = insert(InstrumentAudio.create(instrument36, "Hihat Closed 1 Edge", "2d93990f-a395-4f47-a646-f4f0e9a8c5aa-instrument-36-audio.wav", 0.003000, 0.204000, 121.000000, 537.805000, 0.600000));
    // Instrument Audio Events
    audioEvent928 = insert(InstrumentAudioEvent.create(audio927, 0.000000, 0.500000, "HIHAT", "C5", 1.000000));
    // Instrument Audio Chords
    audio929 = insert(InstrumentAudio.create(instrument36, "Kick 30", "4428cae6-c79a-4f91-9cef-b930bbf68533-instrument-36-audio.wav", 0.000000, 0.473000, 121.000000, 5512.500000, 0.600000));
    // Instrument Audio Events
    audioEvent930 = insert(InstrumentAudioEvent.create(audio929, 0.000000, 1.000000, "KICK", "f8", 1.000000));
    // Instrument Audio Chords
    audio931 = insert(InstrumentAudio.create(instrument36, "Kick 70", "c359546f-7170-4510-9b31-a58bd3b2fceb-instrument-36-audio.wav", 0.000000, 0.294000, 121.000000, 79.032000, 0.600000));
    // Instrument Audio Events
    audioEvent932 = insert(InstrumentAudioEvent.create(audio931, 0.000000, 1.000000, "KICK", "Eb2", 1.000000));
    // Instrument Audio Chords
    audio933 = insert(InstrumentAudio.create(instrument36, "Tom B3", "4a35c285-ed7b-43e7-b37f-32e5441624fb-instrument-36-audio.wav", 0.000000, 1.858000, 121.000000, 104.255000, 0.600000));
    // Instrument Audio Events
    audioEvent934 = insert(InstrumentAudioEvent.create(audio933, 0.000000, 1.000000, "TOM", "Ab2", 1.000000));
    // Instrument Audio Chords
    audio935 = insert(InstrumentAudio.create(instrument36, "Hihat Open 1", "975ce349-7f3e-44bb-988b-69c75511962e-instrument-36-audio.wav", 0.010000, 1.390000, 121.000000, 331.579000, 0.600000));
    // Instrument Audio Events
    audioEvent936 = insert(InstrumentAudioEvent.create(audio935, 0.000000, 1.000000, "HIHATOPEN", "e4", 1.000000));
    // Instrument Audio Chords
    audio937 = insert(InstrumentAudio.create(instrument36, "Hihat Closed 2", "0ca9465e-95f3-4e52-bc7f-6a9176bd7f5a-instrument-36-audio.wav", 0.000000, 0.149000, 121.000000, 7350.000000, 0.600000));
    // Instrument Audio Events
    audioEvent938 = insert(InstrumentAudioEvent.create(audio937, 0.000000, 0.500000, "HIHAT", "Bb8", 1.000000));
    // Instrument Audio Chords
    audio939 = insert(InstrumentAudio.create(instrument36, "Snare 30", "8ddc0c7b-7324-4226-8380-f70d53b7be33-instrument-36-audio.wav", 0.000000, 1.268000, 121.000000, 235.829000, 0.600000));
    // Instrument Audio Events
    audioEvent940 = insert(InstrumentAudioEvent.create(audio939, 0.000000, 1.000000, "SNARE", "Bb3", 1.000000));
    // Instrument Audio Chords
    audio941 = insert(InstrumentAudio.create(instrument36, "Snare 31", "4746ca89-d9f5-4815-a12c-91f4a01e56f4-instrument-36-audio.wav", 0.000000, 1.834000, 121.000000, 175.697000, 0.600000));
    // Instrument Audio Events
    // Instrument Audio Chords
    audio942 = insert(InstrumentAudio.create(instrument36, "Hihat Closed 1", "5cab2c30-f2e2-45c9-a16a-35c90a259d92-instrument-36-audio.wav", 0.001000, 0.172000, 121.000000, 373.729000, 0.600000));
    // Instrument Audio Events
    audioEvent943 = insert(InstrumentAudioEvent.create(audio942, 0.000000, 0.500000, "HIHAT", "Gb4", 1.000000));
    // Instrument Audio Chords
    audio944 = insert(InstrumentAudio.create(instrument36, "Tom B1", "ab2e3e91-cf16-4737-923d-77889852ea54-instrument-36-audio.wav", 0.000000, 1.158000, 121.000000, 148.986000, 0.600000));
    // Instrument Audio Events
    audioEvent945 = insert(InstrumentAudioEvent.create(audio944, 0.000000, 1.000000, "TOM", "D3", 1.000000));
    // Instrument Audio Chords
    audio946 = insert(InstrumentAudio.create(instrument36, "Snare 34", "db961345-3fca-4bed-a297-597a765d535c-instrument-36-audio.wav", 0.000000, 0.462000, 121.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent947 = insert(InstrumentAudioEvent.create(audio946, 0.000000, 1.000000, "SNARE", "g8", 1.000000));
    // Instrument Audio Chords
    audio948 = insert(InstrumentAudio.create(instrument36, "Crash 7", "f4d71032-0066-442a-a102-c13abdf226f1-instrument-36-audio.wav", 0.000000, 3.175000, 121.000000, 678.462000, 0.600000));
    // Instrument Audio Events
    audioEvent949 = insert(InstrumentAudioEvent.create(audio948, 0.000000, 4.000000, "CRASH", "E5", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument Water Basic X
    instrument37 = insert(Instrument.create(user1, library1, "Percussive", "Published", "Water Basic X", 0.000000));
    // Instrument Memes
    // Instrument Audios


    // Insert Percussive-type Instrument Water Large
    instrument31 = insert(Instrument.create(user3, library3, "Percussive", "Published", "Water Large", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument31, "Large"));
    insert(InstrumentMeme.create(instrument31, "Water"));
    // Instrument Audios
    audio950 = insert(InstrumentAudio.create(instrument31, "Kick 3", "cb9f17ef-6206-44a7-85c2-4284b1cbe024-instrument-31-audio.wav", 0.000000, 0.772000, 121.000000, 57.831000, 0.600000));
    // Instrument Audio Events
    audioEvent951 = insert(InstrumentAudioEvent.create(audio950, 0.000000, 1.000000, "KICK", "Bb1", 1.000000));
    // Instrument Audio Chords
    audio952 = insert(InstrumentAudio.create(instrument31, "Tom 8", "5025ecce-8c1c-456b-9934-5927bf3f6b11-instrument-31-audio.wav", 0.000000, 0.945000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent953 = insert(InstrumentAudioEvent.create(audio952, 0.000000, 1.000000, "TOM", "F#8", 1.000000));
    // Instrument Audio Chords
    audio954 = insert(InstrumentAudio.create(instrument31, "Kick 12", "5c09ac3a-524f-4b63-8a86-dd4faa983320-instrument-31-audio.wav", 0.000000, 0.447000, 121.000000, 95.238000, 0.600000));
    // Instrument Audio Events
    audioEvent955 = insert(InstrumentAudioEvent.create(audio954, 0.000000, 1.000000, "KICK", "G2", 1.000000));
    // Instrument Audio Chords
    audio956 = insert(InstrumentAudio.create(instrument31, "Snare 43", "2a8946ae-651c-4af8-a49e-7c4e62e508ee-instrument-31-audio.wav", 0.000000, 1.259000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent957 = insert(InstrumentAudioEvent.create(audio956, 0.000000, 1.000000, "SNARE", "B8", 1.000000));
    // Instrument Audio Chords
    audio958 = insert(InstrumentAudio.create(instrument31, "Kick 9", "fcadf35e-d970-4072-8923-c4f94a3c2386-instrument-31-audio.wav", 0.000000, 0.524000, 121.000000, 125.984000, 0.600000));
    // Instrument Audio Events
    audioEvent959 = insert(InstrumentAudioEvent.create(audio958, 0.000000, 1.000000, "KICK", "B2", 1.000000));
    // Instrument Audio Chords
    audio960 = insert(InstrumentAudio.create(instrument31, "Snare 30", "4a2ea867-86ae-47c5-bf9b-ae9ad542187c-instrument-31-audio.wav", 0.000000, 0.723000, 121.000000, 5333.330000, 0.600000));
    // Instrument Audio Events
    audioEvent961 = insert(InstrumentAudioEvent.create(audio960, 0.000000, 1.000000, "SNARE", "E8", 1.000000));
    // Instrument Audio Chords
    audio962 = insert(InstrumentAudio.create(instrument31, "Kick 24", "545aaa55-07a2-4e00-9a77-07adc564ed8b-instrument-31-audio.wav", 0.000000, 0.524000, 121.000000, 130.790000, 0.600000));
    // Instrument Audio Events
    audioEvent963 = insert(InstrumentAudioEvent.create(audio962, 0.000000, 1.000000, "KICK", "C3", 1.000000));
    // Instrument Audio Chords
    audio964 = insert(InstrumentAudio.create(instrument31, "Snare 9", "fd66964c-b23b-47d2-ad5b-2ce11ee59d7a-instrument-31-audio.wav", 0.000000, 0.539000, 121.000000, 2285.710000, 0.600000));
    // Instrument Audio Events
    audioEvent965 = insert(InstrumentAudioEvent.create(audio964, 0.000000, 1.000000, "SNARE", "D7", 1.000000));
    // Instrument Audio Chords
    audio966 = insert(InstrumentAudio.create(instrument31, "Kick 15", "71dd026c-3ff9-4abf-ac1b-6c6bb6992688-instrument-31-audio.wav", 0.000000, 0.474000, 121.000000, 72.289000, 0.600000));
    // Instrument Audio Events
    audioEvent967 = insert(InstrumentAudioEvent.create(audio966, 0.000000, 1.000000, "KICK", "D2", 1.000000));
    // Instrument Audio Chords
    audio968 = insert(InstrumentAudio.create(instrument31, "Kick 23", "a477e828-9fbe-4827-b601-54413e3dd095-instrument-31-audio.wav", 0.000000, 0.387000, 121.000000, 74.766000, 0.600000));
    // Instrument Audio Events
    audioEvent969 = insert(InstrumentAudioEvent.create(audio968, 0.000000, 1.000000, "KICK", "D2", 1.000000));
    // Instrument Audio Chords
    audio970 = insert(InstrumentAudio.create(instrument31, "Tom 2", "c00bb383-5220-44d8-a6e4-d52d798a5801-instrument-31-audio.wav", 0.000000, 1.150000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent971 = insert(InstrumentAudioEvent.create(audio970, 0.000000, 1.000000, "TOM", "F#8", 1.000000));
    // Instrument Audio Chords
    audio972 = insert(InstrumentAudio.create(instrument31, "Tom 5", "dc724a2a-c208-4fbc-900f-c0e48d7935da-instrument-31-audio.wav", 0.000000, 1.098000, 121.000000, 145.897000, 0.600000));
    // Instrument Audio Events
    audioEvent973 = insert(InstrumentAudioEvent.create(audio972, 0.000000, 1.000000, "TOM", "D3", 1.000000));
    // Instrument Audio Chords
    audio974 = insert(InstrumentAudio.create(instrument31, "Kick 38 ", "96075be4-0c3f-45b6-936a-f5afce156a06-instrument-31-audio.wav", 0.000000, 0.542000, 121.000000, 125.654000, 0.600000));
    // Instrument Audio Events
    audioEvent975 = insert(InstrumentAudioEvent.create(audio974, 0.000000, 1.000000, "KICK", "B2", 1.000000));
    // Instrument Audio Chords
    audio976 = insert(InstrumentAudio.create(instrument31, "Snare 31", "6b035b72-ef01-422c-ae2a-33a5c9433754-instrument-31-audio.wav", 0.000000, 0.687000, 121.000000, 5333.330000, 0.600000));
    // Instrument Audio Events
    audioEvent977 = insert(InstrumentAudioEvent.create(audio976, 0.000000, 1.000000, "SNARE", "E8", 1.000000));
    // Instrument Audio Chords
    audio978 = insert(InstrumentAudio.create(instrument31, "Kick 27", "4bf621f4-84fd-4b9f-aab8-5a5758405422-instrument-31-audio.wav", 0.000000, 0.447000, 121.000000, 95.238000, 0.600000));
    // Instrument Audio Events
    audioEvent979 = insert(InstrumentAudioEvent.create(audio978, 0.000000, 1.000000, "KICK", "G2", 1.000000));
    // Instrument Audio Chords
    audio980 = insert(InstrumentAudio.create(instrument31, "Kick 28", "f2c959fe-9574-4a72-b313-2a1fcdf4dda3-instrument-31-audio.wav", 0.000000, 0.432000, 121.000000, 72.948000, 0.600000));
    // Instrument Audio Events
    audioEvent981 = insert(InstrumentAudioEvent.create(audio980, 0.000000, 1.000000, "KICK", "D2", 1.000000));
    // Instrument Audio Chords
    audio982 = insert(InstrumentAudio.create(instrument31, "Kick 14", "031c2f89-ff98-42e9-8f65-77c8ea901379-instrument-31-audio.wav", 0.000000, 0.415000, 121.000000, 178.439000, 0.600000));
    // Instrument Audio Events
    audioEvent983 = insert(InstrumentAudioEvent.create(audio982, 0.000000, 1.000000, "KICK", "F3", 1.000000));
    // Instrument Audio Chords
    audio984 = insert(InstrumentAudio.create(instrument31, "Snare 28", "f91482d8-14d9-4ffd-b0bd-035259397cc2-instrument-31-audio.wav", 0.000000, 0.645000, 121.000000, 2526.320000, 0.600000));
    // Instrument Audio Events
    audioEvent985 = insert(InstrumentAudioEvent.create(audio984, 0.000000, 1.000000, "SNARE", "D#7", 1.000000));
    // Instrument Audio Chords
    audio986 = insert(InstrumentAudio.create(instrument31, "Kick 21", "a7ec1314-d3ff-4bbe-9884-b050beeb0709-instrument-31-audio.wav", 0.000000, 0.766000, 121.000000, 97.959000, 0.600000));
    // Instrument Audio Events
    audioEvent987 = insert(InstrumentAudioEvent.create(audio986, 0.000000, 1.000000, "KICK", "G2", 1.000000));
    // Instrument Audio Chords
    audio988 = insert(InstrumentAudio.create(instrument31, "Fluxing Shaker 1", "0aee8863-13d1-4e88-877d-5b2d90d780ee-instrument-31-audio.wav", 0.000000, 0.306000, 121.000000, 1411.770000, 0.600000));
    // Instrument Audio Events
    audioEvent989 = insert(InstrumentAudioEvent.create(audio988, 0.000000, 1.000000, "TOM", "F6", 1.000000));
    // Instrument Audio Chords
    audio990 = insert(InstrumentAudio.create(instrument31, "Kick 35", "22c461ab-99be-4aa7-9982-db3e890696cd-instrument-31-audio.wav", 0.000000, 0.766000, 121.000000, 94.488000, 0.600000));
    // Instrument Audio Events
    audioEvent991 = insert(InstrumentAudioEvent.create(audio990, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords
    audio992 = insert(InstrumentAudio.create(instrument31, "Kick 31", "ca39f467-0a37-4f48-999c-34ca8d0dfee7-instrument-31-audio.wav", 0.000000, 0.416000, 121.000000, 59.553000, 0.600000));
    // Instrument Audio Events
    audioEvent993 = insert(InstrumentAudioEvent.create(audio992, 0.000000, 1.000000, "KICK", "A#1", 1.000000));
    // Instrument Audio Chords
    audio994 = insert(InstrumentAudio.create(instrument31, "Tom 3", "89614224-5284-4c70-86f5-6aec8edca200-instrument-31-audio.wav", 0.000000, 1.100000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent995 = insert(InstrumentAudioEvent.create(audio994, 0.000000, 1.000000, "TOM", "F#8", 1.000000));
    // Instrument Audio Chords
    audio996 = insert(InstrumentAudio.create(instrument31, "Fluxing Shaker 2", "102fe87b-767e-47a5-9760-32d29cd1cf3f-instrument-31-audio.wav", 0.000000, 0.363000, 121.000000, 1263.160000, 0.600000));
    // Instrument Audio Events
    audioEvent997 = insert(InstrumentAudioEvent.create(audio996, 0.000000, 1.000000, "TOM", "D#6", 1.000000));
    // Instrument Audio Chords
    audio998 = insert(InstrumentAudio.create(instrument31, "Kick 11", "e24863e9-9d74-4113-9302-a99a8d88928a-instrument-31-audio.wav", 0.000000, 0.396000, 121.000000, 100.000000, 0.600000));
    // Instrument Audio Events
    audioEvent999 = insert(InstrumentAudioEvent.create(audio998, 0.000000, 1.000000, "KICK", "G2", 1.000000));
    // Instrument Audio Chords
    audio1000 = insert(InstrumentAudio.create(instrument31, "Snare 29", "2c8b8474-f459-4621-8aee-26a0c492f124-instrument-31-audio.wav", 0.000000, 0.585000, 121.000000, 5333.330000, 0.600000));
    // Instrument Audio Events
    audioEvent1001 = insert(InstrumentAudioEvent.create(audio1000, 0.000000, 1.000000, "SNARE", "E8", 1.000000));
    // Instrument Audio Chords
    audio1002 = insert(InstrumentAudio.create(instrument31, "Kick 36", "021d253b-f0fd-4858-9e51-4ce89318ba83-instrument-31-audio.wav", 0.000000, 0.678000, 121.000000, 86.799000, 0.600000));
    // Instrument Audio Events
    audioEvent1003 = insert(InstrumentAudioEvent.create(audio1002, 0.000000, 1.000000, "KICK", "F2", 1.000000));
    // Instrument Audio Chords
    audio1004 = insert(InstrumentAudio.create(instrument31, "Snare 23", "b932198b-2329-461a-9fd4-73abd928b826-instrument-31-audio.wav", 0.000000, 0.546000, 121.000000, 9428.570000, 0.600000));
    // Instrument Audio Events
    audioEvent1005 = insert(InstrumentAudioEvent.create(audio1004, 0.000000, 1.000000, "SNARE", "A7", 1.000000));
    // Instrument Audio Chords
    audio1006 = insert(InstrumentAudio.create(instrument31, "Kick 7", "4c2b9e01-68fb-412e-b0c2-d0407efb05e1-instrument-31-audio.wav", 0.000000, 0.813000, 121.000000, 57.831000, 0.600000));
    // Instrument Audio Events
    audioEvent1007 = insert(InstrumentAudioEvent.create(audio1006, 0.000000, 1.000000, "KICK", "A#1", 1.000000));
    // Instrument Audio Chords
    audio1008 = insert(InstrumentAudio.create(instrument31, "Tom 6", "a81c1ddb-7911-4f60-a67a-74842c3f7f2c-instrument-31-audio.wav", 0.000000, 1.157000, 121.000000, 6857.130000, 0.600000));
    // Instrument Audio Events
    audioEvent1009 = insert(InstrumentAudioEvent.create(audio1008, 0.000000, 1.000000, "TOM", "A8", 1.000000));
    // Instrument Audio Chords
    audio1010 = insert(InstrumentAudio.create(instrument31, "Snare 36", "e3366e98-1be0-4de7-94f0-e2ad666102f3-instrument-31-audio.wav", 0.000000, 0.849000, 121.000000, 3000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1011 = insert(InstrumentAudioEvent.create(audio1010, 0.000000, 1.000000, "SNARE", "F#7", 1.000000));
    // Instrument Audio Chords
    audio1012 = insert(InstrumentAudio.create(instrument31, "Kick 19", "893cfb4b-69b8-4b16-a042-931b5368a045-instrument-31-audio.wav", 0.000000, 0.418000, 121.000000, 88.725000, 0.600000));
    // Instrument Audio Events
    audioEvent1013 = insert(InstrumentAudioEvent.create(audio1012, 0.000000, 1.000000, "KICK", "F2", 1.000000));
    // Instrument Audio Chords
    audio1014 = insert(InstrumentAudio.create(instrument31, "Kick 37", "d756bd4e-af20-4257-8f89-d2e6655bf6d5-instrument-31-audio.wav", 0.000000, 0.387000, 121.000000, 86.957000, 0.600000));
    // Instrument Audio Events
    audioEvent1015 = insert(InstrumentAudioEvent.create(audio1014, 0.000000, 1.000000, "KICK", "F2", 1.000000));
    // Instrument Audio Chords
    audio1016 = insert(InstrumentAudio.create(instrument31, "Hi-Hat 1", "ab8c8732-1f9e-4df3-863a-6855c4ed5c66-instrument-31-audio.wav", 0.000000, 0.068000, 121.000000, 16000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1017 = insert(InstrumentAudioEvent.create(audio1016, 0.000000, 1.000000, "HIHAT", "B9", 1.000000));
    // Instrument Audio Chords
    audio1018 = insert(InstrumentAudio.create(instrument31, "Kick 13", "c557059c-3191-443a-ab80-d19f572cc1ce-instrument-31-audio.wav", 0.000000, 0.432000, 121.000000, 78.049000, 0.600000));
    // Instrument Audio Events
    audioEvent1019 = insert(InstrumentAudioEvent.create(audio1018, 0.000000, 1.000000, "KICK", "D#2", 1.000000));
    // Instrument Audio Chords
    audio1020 = insert(InstrumentAudio.create(instrument31, "Snare 14", "f710d993-30b5-4c3c-9839-c1cbbc6df68f-instrument-31-audio.wav", 0.125000, 0.671000, 121.000000, 143.713000, 0.600000));
    // Instrument Audio Events
    audioEvent1021 = insert(InstrumentAudioEvent.create(audio1020, 0.000000, 1.000000, "SNARE", "D3", 1.000000));
    // Instrument Audio Chords
    audio1022 = insert(InstrumentAudio.create(instrument31, "Kick 33", "83abe818-f137-4a36-a909-101be2736d1c-instrument-31-audio.wav", 0.000000, 0.442000, 121.000000, 60.606000, 0.600000));
    // Instrument Audio Events
    audioEvent1023 = insert(InstrumentAudioEvent.create(audio1022, 0.000000, 1.000000, "KICK", "B1", 1.000000));
    // Instrument Audio Chords
    audio1024 = insert(InstrumentAudio.create(instrument31, "Snare 16", "9f5911e7-5a31-4d89-9eee-2104b9af0d61-instrument-31-audio.wav", 0.000000, 0.543000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1025 = insert(InstrumentAudioEvent.create(audio1024, 0.000000, 1.000000, "SNARE", "F#8", 1.000000));
    // Instrument Audio Chords
    audio1026 = insert(InstrumentAudio.create(instrument31, "Snare 2", "90e24308-fc3f-4931-90e2-2589400e9a20-instrument-31-audio.wav", 0.000000, 0.527000, 121.000000, 173.285000, 0.600000));
    // Instrument Audio Events
    audioEvent1027 = insert(InstrumentAudioEvent.create(audio1026, 0.000000, 1.000000, "SNARE", "F3", 1.000000));
    // Instrument Audio Chords
    audio1028 = insert(InstrumentAudio.create(instrument31, "Snare 35", "ec7e1863-cec0-479b-b242-3f6779d62510-instrument-31-audio.wav", 0.000000, 0.589000, 121.000000, 3200.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1029 = insert(InstrumentAudioEvent.create(audio1028, 0.000000, 1.000000, "SNARE", "G7", 1.000000));
    // Instrument Audio Chords
    audio1030 = insert(InstrumentAudio.create(instrument31, "Tom 10", "949e0b37-4657-4d5a-8622-776017c0cbea-instrument-31-audio.wav", 0.000000, 0.956000, 121.000000, 149.068000, 0.600000));
    // Instrument Audio Events
    audioEvent1031 = insert(InstrumentAudioEvent.create(audio1030, 0.000000, 1.000000, "TOM", "D3", 1.000000));
    // Instrument Audio Chords
    audio1032 = insert(InstrumentAudio.create(instrument31, "Tom 7", "e281a5c1-6abf-4c4b-9fe5-7b039364bb82-instrument-31-audio.wav", 0.000000, 1.123000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1033 = insert(InstrumentAudioEvent.create(audio1032, 0.000000, 1.000000, "TOM", "F#8", 1.000000));
    // Instrument Audio Chords
    audio1034 = insert(InstrumentAudio.create(instrument31, "Kick 26", "a1c52a13-e7a4-49bf-854f-8f2368305b3f-instrument-31-audio.wav", 0.000000, 0.405000, 121.000000, 114.286000, 0.600000));
    // Instrument Audio Events
    audioEvent1035 = insert(InstrumentAudioEvent.create(audio1034, 0.000000, 1.000000, "KICK", "A#2", 1.000000));
    // Instrument Audio Chords
    audio1036 = insert(InstrumentAudio.create(instrument31, "Snare 13", "954655d4-f738-41bc-8b84-9996bf28db9c-instrument-31-audio.wav", 0.000000, 0.697000, 121.000000, 4000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1037 = insert(InstrumentAudioEvent.create(audio1036, 0.000000, 1.000000, "SNARE", "B7", 1.000000));
    // Instrument Audio Chords
    audio1038 = insert(InstrumentAudio.create(instrument31, "Tom 9", "8fc44d96-038f-406c-9709-0b6880087c91-instrument-31-audio.wav", 0.000000, 1.299000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1039 = insert(InstrumentAudioEvent.create(audio1038, 0.000000, 1.000000, "TOM", "B8", 1.000000));
    // Instrument Audio Chords
    audio1040 = insert(InstrumentAudio.create(instrument31, "Snare 21", "da2c17a7-1713-4070-a7ba-a5d17207458f-instrument-31-audio.wav", 0.000000, 0.518000, 121.000000, 2086.960000, 0.600000));
    // Instrument Audio Events
    audioEvent1041 = insert(InstrumentAudioEvent.create(audio1040, 0.000000, 1.000000, "SNARE", "C7", 1.000000));
    // Instrument Audio Chords
    audio1042 = insert(InstrumentAudio.create(instrument31, "Kick 17", "b35315e5-1c4f-43f2-afcc-be3c2ab1abbf-instrument-31-audio.wav", 0.000000, 0.474000, 121.000000, 72.289000, 0.600000));
    // Instrument Audio Events
    audioEvent1043 = insert(InstrumentAudioEvent.create(audio1042, 0.000000, 1.000000, "KICK", "D2", 1.000000));
    // Instrument Audio Chords
    audio1044 = insert(InstrumentAudio.create(instrument31, "Snare 3", "d3ae2e3f-8bb8-4d5a-b3a6-a5e63f3abd3d-instrument-31-audio.wav", 0.000000, 0.532000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1045 = insert(InstrumentAudioEvent.create(audio1044, 0.000000, 1.000000, "SNARE", "F#8", 1.000000));
    // Instrument Audio Chords
    audio1046 = insert(InstrumentAudio.create(instrument31, "Tom 1", "24df9e2a-8f23-4d4a-93fb-7d491d090c70-instrument-31-audio.wav", 0.000000, 1.040000, 121.000000, 5333.330000, 0.600000));
    // Instrument Audio Events
    audioEvent1047 = insert(InstrumentAudioEvent.create(audio1046, 0.000000, 1.000000, "TOM", "E8", 1.000000));
    // Instrument Audio Chords
    audio1048 = insert(InstrumentAudio.create(instrument31, "Snare 8", "452eef7e-e662-4599-9dd5-9a65acd04364-instrument-31-audio.wav", 0.000000, 0.525000, 121.000000, 2086.960000, 0.600000));
    // Instrument Audio Events
    audioEvent1049 = insert(InstrumentAudioEvent.create(audio1048, 0.000000, 1.000000, "SNARE", "C7", 1.000000));
    // Instrument Audio Chords
    audio1050 = insert(InstrumentAudio.create(instrument31, "Snare 15", "37afd183-9742-40ce-97c8-b8f92ff9ed5e-instrument-31-audio.wav", 0.000000, 0.529000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1051 = insert(InstrumentAudioEvent.create(audio1050, 0.000000, 1.000000, "SNARE", "D8", 1.000000));
    // Instrument Audio Chords
    audio1052 = insert(InstrumentAudio.create(instrument31, "Kick 20", "29eac327-79af-46d6-bdc9-7c04e9591f66-instrument-31-audio.wav", 0.000000, 0.512000, 121.000000, 95.050000, 0.600000));
    // Instrument Audio Events
    audioEvent1053 = insert(InstrumentAudioEvent.create(audio1052, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords
    audio1054 = insert(InstrumentAudio.create(instrument31, "Kick 29", "df0543c8-9a0b-4942-97d5-87c4e7910e46-instrument-31-audio.wav", 0.000000, 0.415000, 121.000000, 83.189000, 0.600000));
    // Instrument Audio Events
    audioEvent1055 = insert(InstrumentAudioEvent.create(audio1054, 0.000000, 1.000000, "KICK", "E2", 1.000000));
    // Instrument Audio Chords
    audio1056 = insert(InstrumentAudio.create(instrument31, "Snare 38", "52dbfd5d-3c6f-43d7-9476-065533534726-instrument-31-audio.wav", 0.000000, 0.850000, 121.000000, 3200.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1057 = insert(InstrumentAudioEvent.create(audio1056, 0.000000, 1.000000, "SNARE", "G7", 1.000000));
    // Instrument Audio Chords
    audio1058 = insert(InstrumentAudio.create(instrument31, "Snare 1", "900653d0-335d-4861-b615-b2ec1e878150-instrument-31-audio.wav", 0.000000, 0.542000, 121.000000, 3692.310000, 0.600000));
    // Instrument Audio Events
    audioEvent1059 = insert(InstrumentAudioEvent.create(audio1058, 0.000000, 1.000000, "SNARE", "A#7", 1.000000));
    // Instrument Audio Chords
    audio1060 = insert(InstrumentAudio.create(instrument31, "Snare 4", "582674b4-7768-4d79-9cea-ce2142112cb8-instrument-31-audio.wav", 0.000000, 0.581000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1061 = insert(InstrumentAudioEvent.create(audio1060, 0.000000, 1.000000, "SNARE", "F#8", 1.000000));
    // Instrument Audio Chords
    audio1062 = insert(InstrumentAudio.create(instrument31, "Snare 24", "8e5b0601-e894-4997-a69d-819a012d1aec-instrument-31-audio.wav", 0.000000, 0.546000, 121.000000, 3428.570000, 0.600000));
    // Instrument Audio Events
    audioEvent1063 = insert(InstrumentAudioEvent.create(audio1062, 0.000000, 1.000000, "SNARE", "A7", 1.000000));
    // Instrument Audio Chords
    audio1064 = insert(InstrumentAudio.create(instrument31, "Kick 8", "336b3234-55e7-4388-8598-49a1198c3d55-instrument-31-audio.wav", 0.000000, 0.387000, 121.000000, 109.840000, 0.600000));
    // Instrument Audio Events
    audioEvent1065 = insert(InstrumentAudioEvent.create(audio1064, 0.000000, 1.000000, "KICK", "A2", 1.000000));
    // Instrument Audio Chords
    audio1066 = insert(InstrumentAudio.create(instrument31, "Kick 5", "ff18ad0a-73fb-48fc-99cf-fcde25e547eb-instrument-31-audio.wav", 0.000000, 0.762000, 121.000000, 93.385000, 0.600000));
    // Instrument Audio Events
    audioEvent1067 = insert(InstrumentAudioEvent.create(audio1066, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords
    audio1068 = insert(InstrumentAudio.create(instrument31, "Crash 9", "d5a38675-7468-49b0-8014-05bf4a51aa6d-instrument-31-audio.wav", 0.018000, 2.390000, 121.000000, 857.153000, 0.600000));
    // Instrument Audio Events
    audioEvent1069 = insert(InstrumentAudioEvent.create(audio1068, 0.000000, 4.000000, "CRASH", "A5", 1.000000));
    // Instrument Audio Chords
    audio1070 = insert(InstrumentAudio.create(instrument31, "Kick 16", "3b80e6af-512f-41ac-849e-691b562a2f4c-instrument-31-audio.wav", 0.000000, 0.416000, 121.000000, 79.077000, 0.600000));
    // Instrument Audio Events
    audioEvent1071 = insert(InstrumentAudioEvent.create(audio1070, 0.000000, 1.000000, "KICK", "D#2", 1.000000));
    // Instrument Audio Chords
    audio1072 = insert(InstrumentAudio.create(instrument31, "Tom 4", "a5c2ef5d-e593-42ee-a275-a964117539ba-instrument-31-audio.wav", 0.000000, 1.014000, 121.000000, 141.176000, 0.600000));
    // Instrument Audio Events
    audioEvent1073 = insert(InstrumentAudioEvent.create(audio1072, 0.000000, 1.000000, "TOM", "C#3", 1.000000));
    // Instrument Audio Chords
    audio1074 = insert(InstrumentAudio.create(instrument31, "Snare 42", "6b507b9b-5446-4835-912a-b72f87e520f9-instrument-31-audio.wav", 0.003000, 0.811000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1075 = insert(InstrumentAudioEvent.create(audio1074, 0.000000, 1.000000, "SNARE", "E8", 1.000000));
    // Instrument Audio Chords
    audio1076 = insert(InstrumentAudio.create(instrument31, "Snare 44", "f6e0c316-68ed-4e6e-a4f9-113817076dfd-instrument-31-audio.wav", 0.000000, 0.694000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1077 = insert(InstrumentAudioEvent.create(audio1076, 0.000000, 1.000000, "SNARE", "F#8", 1.000000));
    // Instrument Audio Chords
    audio1078 = insert(InstrumentAudio.create(instrument31, "Crash 11", "e6044e66-1da3-4639-86ad-b797f9ede600-instrument-31-audio.wav", 0.081000, 2.483000, 121.000000, 501.136000, 0.600000));
    // Instrument Audio Events
    audioEvent1079 = insert(InstrumentAudioEvent.create(audio1078, 0.000000, 4.000000, "CRASH", "B4", 1.000000));
    // Instrument Audio Chords
    audio1080 = insert(InstrumentAudio.create(instrument31, "Snare 7", "fe7d0229-0833-47f7-9253-40e2066b3176-instrument-31-audio.wav", 0.000000, 0.719000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1081 = insert(InstrumentAudioEvent.create(audio1080, 0.000000, 1.000000, "SNARE", "F#8", 1.000000));
    // Instrument Audio Chords
    audio1082 = insert(InstrumentAudio.create(instrument31, "Kick 18", "8fafb486-38fd-4676-aa80-6984beab0cf3-instrument-31-audio.wav", 0.000000, 0.442000, 121.000000, 77.295000, 0.600000));
    // Instrument Audio Events
    audioEvent1083 = insert(InstrumentAudioEvent.create(audio1082, 0.000000, 1.000000, "KICK", "D#2", 1.000000));
    // Instrument Audio Chords
    audio1084 = insert(InstrumentAudio.create(instrument31, "Kick 32", "e09a0f7b-113b-4f09-a992-953aacf34248-instrument-31-audio.wav", 0.000000, 0.418000, 121.000000, 98.765000, 0.600000));
    // Instrument Audio Events
    audioEvent1085 = insert(InstrumentAudioEvent.create(audio1084, 0.000000, 1.000000, "KICK", "G2", 1.000000));
    // Instrument Audio Chords
    audio1086 = insert(InstrumentAudio.create(instrument31, "Kick 22", "997c368d-5537-4668-833d-06001bbf8d87-instrument-31-audio.wav", 0.000000, 0.813000, 121.000000, 60.150000, 0.600000));
    // Instrument Audio Events
    audioEvent1087 = insert(InstrumentAudioEvent.create(audio1086, 0.000000, 1.000000, "KICK", "B1", 1.000000));
    // Instrument Audio Chords
    audio1088 = insert(InstrumentAudio.create(instrument31, "Kick 25", "0d957165-32ed-4bc0-b1ec-7e7319f4ae70-instrument-31-audio.wav", 0.000000, 0.442000, 121.000000, 183.206000, 0.600000));
    // Instrument Audio Events
    audioEvent1089 = insert(InstrumentAudioEvent.create(audio1088, 0.000000, 1.000000, "KICK", "F#3", 1.000000));
    // Instrument Audio Chords
    audio1090 = insert(InstrumentAudio.create(instrument31, "Kick 30", "e974711c-0d28-40d7-a5a1-22420b43ae98-instrument-31-audio.wav", 0.000000, 0.474000, 121.000000, 93.567000, 0.600000));
    // Instrument Audio Events
    audioEvent1091 = insert(InstrumentAudioEvent.create(audio1090, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords
    audio1092 = insert(InstrumentAudio.create(instrument31, "Kick 6", "70175671-9e0a-4802-a442-cec930df1e13-instrument-31-audio.wav", 0.000000, 0.678000, 121.000000, 90.226000, 0.600000));
    // Instrument Audio Events
    audioEvent1093 = insert(InstrumentAudioEvent.create(audio1092, 0.000000, 1.000000, "KICK", "F#2", 1.000000));
    // Instrument Audio Chords
    audio1094 = insert(InstrumentAudio.create(instrument31, "Kick 2", "ab6c63c0-06e2-4bbe-afc7-f38fe4eba838-instrument-31-audio.wav", 0.000000, 0.380000, 121.000000, 109.091000, 0.600000));
    // Instrument Audio Events
    audioEvent1095 = insert(InstrumentAudioEvent.create(audio1094, 0.000000, 1.000000, "KICK", "A2", 1.000000));
    // Instrument Audio Chords
    audio1096 = insert(InstrumentAudio.create(instrument31, "Snare 34", "e75783da-652f-470b-ac41-347bcf6c2c80-instrument-31-audio.wav", 0.000000, 0.773000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1097 = insert(InstrumentAudioEvent.create(audio1096, 0.000000, 1.000000, "SNARE", "F#8", 1.000000));
    // Instrument Audio Chords

  }

  private void go8() throws Exception {
    // Insert Percussive-type Instrument Water Small
    instrument27 = insert(Instrument.create(user3, library3, "Percussive", "Published", "Water Small", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument27, "Water"));
    insert(InstrumentMeme.create(instrument27, "Small"));
    // Instrument Audios
    audio1098 = insert(InstrumentAudio.create(instrument27, "Clave w/ Reverb", "8d02968d-1453-42db-a80d-fbca37f2997b-instrument-27-audio.wav", 0.000000, 1.750000, 121.000000, 2823.530000, 0.600000));
    // Instrument Audio Events
    audioEvent1099 = insert(InstrumentAudioEvent.create(audio1098, 0.000000, 1.000000, "TOM", "F7", 1.000000));
    // Instrument Audio Chords
    audio1100 = insert(InstrumentAudio.create(instrument27, "Hi-Hat 1", "1c85dcd2-d3c8-4dbb-8d9a-60a41c88f4f9-instrument-27-audio.wav", 0.000000, 0.068000, 121.000000, 16000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1101 = insert(InstrumentAudioEvent.create(audio1100, 0.000000, 1.000000, "HIHAT", "B9", 1.000000));
    // Instrument Audio Chords
    audio1102 = insert(InstrumentAudio.create(instrument27, "Spacey Shaker", "2b7286b1-fb7f-406e-8b94-9a0d44506860-instrument-27-audio.wav", 0.000000, 1.750000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1103 = insert(InstrumentAudioEvent.create(audio1102, 0.000000, 1.000000, "HIHATOPEN", "D8", 1.000000));
    // Instrument Audio Chords
    audio1104 = insert(InstrumentAudio.create(instrument27, "Hi-Hat Dry", "f4f989e9-fd15-48ca-9795-57bcfa72783d-instrument-27-audio.wav", 0.000200, 0.500000, 121.000000, 3692.310000, 0.600000));
    // Instrument Audio Events
    audioEvent1105 = insert(InstrumentAudioEvent.create(audio1104, 0.000000, 1.000000, "HIHATOPEN", "Bb7", 1.000000));
    // Instrument Audio Chords
    audio1106 = insert(InstrumentAudio.create(instrument27, "Knocky Rim Click w/ Short Reverb Tail", "3e646e1a-85c0-46b6-947c-a054689c07a2-instrument-27-audio.wav", 0.000300, 1.250000, 121.000000, 251.309000, 0.600000));
    // Instrument Audio Events
    audioEvent1107 = insert(InstrumentAudioEvent.create(audio1106, 0.000000, 1.000000, "SNARE", "B3", 1.000000));
    // Instrument Audio Chords
    audio1108 = insert(InstrumentAudio.create(instrument27, "Springy Clap", "78e1797e-cd64-423d-ba8f-2183edeae8d6-instrument-27-audio.wav", 0.001100, 2.750000, 121.000000, 623.377000, 0.600000));
    // Instrument Audio Events
    audioEvent1109 = insert(InstrumentAudioEvent.create(audio1108, 0.000000, 1.000000, "SNARE", "Eb5", 1.000000));
    // Instrument Audio Chords
    audio1110 = insert(InstrumentAudio.create(instrument27, "Crash 2", "562c03f4-0f3a-406f-a9b7-2da4c26e2d77-instrument-27-audio.wav", 0.007000, 1.225000, 121.000000, 245.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1111 = insert(InstrumentAudioEvent.create(audio1110, 0.000000, 4.000000, "CRASH", "B3", 1.000000));
    // Instrument Audio Chords
    audio1112 = insert(InstrumentAudio.create(instrument27, "Spacey Clave", "3091be9a-37c1-4c2c-bb4e-12d076584838-instrument-27-audio.wav", 0.000000, 2.375000, 121.000000, 2823.530000, 0.600000));
    // Instrument Audio Events
    audioEvent1113 = insert(InstrumentAudioEvent.create(audio1112, 0.000000, 1.000000, "TOM", "F7", 1.000000));
    // Instrument Audio Chords
    audio1114 = insert(InstrumentAudio.create(instrument27, "Spacey Cowbell Low", "81cef961-c5fc-4952-ade8-b7e0fccdd7e0-instrument-27-audio.wav", 0.000000, 2.500000, 121.000000, 452.830000, 0.600000));
    // Instrument Audio Events
    audioEvent1115 = insert(InstrumentAudioEvent.create(audio1114, 0.000000, 1.000000, "TOM", "A4", 1.000000));
    // Instrument Audio Chords
    audio1116 = insert(InstrumentAudio.create(instrument27, "Dry 808 Snare", "0b4c697a-bdd4-4f7c-a0c9-91a74f0e493a-instrument-27-audio.wav", 0.000000, 0.250000, 121.000000, 192.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1117 = insert(InstrumentAudioEvent.create(audio1116, 0.000000, 1.000000, "SNARE", "G3", 1.000000));
    // Instrument Audio Chords
    audio1118 = insert(InstrumentAudio.create(instrument27, "Hi-Hat Open w/ Reverb", "0388acb2-a8ea-4e4f-a3b3-907153ba4ec1-instrument-27-audio.wav", 0.000000, 2.125000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1119 = insert(InstrumentAudioEvent.create(audio1118, 0.000000, 1.000000, "CRASH", "D8", 1.000000));
    // Instrument Audio Chords
    audio1120 = insert(InstrumentAudio.create(instrument27, "Tambourine", "8335ff25-b151-4ef3-a2c9-56dad8f2a2de-instrument-27-audio.wav", 0.000000, 2.125000, 121.000000, 2823.530000, 0.600000));
    // Instrument Audio Events
    audioEvent1121 = insert(InstrumentAudioEvent.create(audio1120, 0.000000, 1.000000, "TOM", "F7", 1.000000));
    // Instrument Audio Chords
    audio1122 = insert(InstrumentAudio.create(instrument27, "Knocky Open Kick", "75baff55-6fc4-4370-8d97-d0560de5c9b6-instrument-27-audio.wav", 0.004000, 0.750000, 121.000000, 127.321000, 0.600000));
    // Instrument Audio Events
    audioEvent1123 = insert(InstrumentAudioEvent.create(audio1122, 0.000000, 1.000000, "KICK", "C3", 1.000000));
    // Instrument Audio Chords
    audio1124 = insert(InstrumentAudio.create(instrument27, "Springy Clap 2: Return of the Springy Clap", "a16ab5e9-0544-4430-9d96-acdfe82c8389-instrument-27-audio.wav", 0.000500, 2.000000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1125 = insert(InstrumentAudioEvent.create(audio1124, 0.000000, 1.000000, "SNARE", "B8", 1.000000));
    // Instrument Audio Chords
    audio1126 = insert(InstrumentAudio.create(instrument27, "Future Shaker", "6913764f-e321-48ee-965d-381f9bca9662-instrument-27-audio.wav", 0.000000, 0.257000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1127 = insert(InstrumentAudioEvent.create(audio1126, 0.000000, 1.000000, "TOM", "F#9", 1.000000));
    // Instrument Audio Chords
    audio1128 = insert(InstrumentAudio.create(instrument27, "Spacey Cowbell High", "2174c84d-c6f0-4acc-be17-b70f7932e014-instrument-27-audio.wav", 0.000000, 2.625000, 121.000000, 705.882000, 0.600000));
    // Instrument Audio Events
    audioEvent1129 = insert(InstrumentAudioEvent.create(audio1128, 0.000000, 1.000000, "TOM", "F5", 1.000000));
    // Instrument Audio Chords
    audio1130 = insert(InstrumentAudio.create(instrument27, "Hi-Hat w/ Reverb", "9a8cd7f2-f68c-4425-8233-c6d77ed2cfcb-instrument-27-audio.wav", 0.000200, 2.500000, 121.000000, 3692.310000, 0.600000));
    // Instrument Audio Events
    audioEvent1131 = insert(InstrumentAudioEvent.create(audio1130, 0.000000, 1.000000, "CRASH", "Bb7", 1.000000));
    // Instrument Audio Chords
    audio1132 = insert(InstrumentAudio.create(instrument27, "Snare w/ Reverb Tail", "6cce584f-d2a3-42ac-95e0-03f3656b5c79-instrument-27-audio.wav", 0.000000, 1.000000, 121.000000, 2400.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1133 = insert(InstrumentAudioEvent.create(audio1132, 0.000000, 1.000000, "SNARE", "D7", 1.000000));
    // Instrument Audio Chords
    audio1134 = insert(InstrumentAudio.create(instrument27, "Rim Click w/ Long Reverb Tail", "3d3d1672-ee60-48bd-b7cf-01ce33d040e2-instrument-27-audio.wav", 0.000200, 2.125000, 121.000000, 400.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1135 = insert(InstrumentAudioEvent.create(audio1134, 0.000000, 1.000000, "SNARE", "G4", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument Wind A (legacy)
    instrument38 = insert(Instrument.create(user1, library1, "Percussive", "Published", "Wind A (legacy)", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument38, "Wind"));
    // Instrument Audios
    audio1136 = insert(InstrumentAudio.create(instrument38, "Koto", "075b432c-3616-4737-a841-033284cf1252-instrument-38-audio.wav", 0.000000, 1.294000, 88.000000, 132.831000, 0.600000));
    // Instrument Audio Events
    audioEvent1137 = insert(InstrumentAudioEvent.create(audio1136, 0.000000, 1.000000, "CRASH", "C3", 1.000000));
    // Instrument Audio Chords
    audio1138 = insert(InstrumentAudio.create(instrument38, "Snare Rim 6", "eedf7a0f-64b2-4c71-8ea8-ba0c75425c8e-instrument-38-audio.wav", 0.000000, 0.358000, 120.000000, 5512.500000, 0.600000));
    // Instrument Audio Events
    audioEvent1139 = insert(InstrumentAudioEvent.create(audio1138, 0.000000, 1.000000, "SNARE", "F8", 1.000000));
    // Instrument Audio Chords
    audio1140 = insert(InstrumentAudio.create(instrument38, "Kalimba", "a825b767-773c-49fe-b1eb-5d62867bc092-instrument-38-audio.wav", 0.000000, 1.175000, 88.000000, 262.500000, 0.600000));
    // Instrument Audio Events
    audioEvent1141 = insert(InstrumentAudioEvent.create(audio1140, 0.000000, 2.000000, "CRASH", "C4", 1.000000));
    // Instrument Audio Chords
    audio1142 = insert(InstrumentAudio.create(instrument38, "Shamisen", "5dcc49f0-fcb4-4f63-942d-d7a0b09aa351-instrument-38-audio.wav", 0.000000, 1.000000, 88.000000, 262.500000, 0.600000));
    // Instrument Audio Events
    audioEvent1143 = insert(InstrumentAudioEvent.create(audio1142, 0.000000, 1.000000, "CRASH", "C4", 1.000000));
    // Instrument Audio Chords
    audio1144 = insert(InstrumentAudio.create(instrument38, "Kick 92", "b6eea389-d360-4604-9b15-0435ba573e2a-instrument-38-audio.wav", 0.000000, 1.463000, 120.000000, 63.728000, 0.600000));
    // Instrument Audio Events
    audioEvent1145 = insert(InstrumentAudioEvent.create(audio1144, 0.000000, 1.000000, "KICK", "C2", 1.000000));
    // Instrument Audio Chords
    audio1146 = insert(InstrumentAudio.create(instrument38, "Kick 93", "576f3155-d321-475d-a5b8-494b9fae6f44-instrument-38-audio.wav", 0.000000, 1.831000, 120.000000, 106.010000, 0.600000));
    // Instrument Audio Events
    audioEvent1147 = insert(InstrumentAudioEvent.create(audio1146, 0.000000, 1.000000, "KICK", "Ab2", 1.000000));
    // Instrument Audio Chords
    audio1148 = insert(InstrumentAudio.create(instrument38, "Crash 14", "4493b949-9bc2-44a2-9d74-c65d5b96587c-instrument-38-audio.wav", 0.000000, 1.479000, 121.000000, 518.824000, 0.600000));
    // Instrument Audio Events
    audioEvent1149 = insert(InstrumentAudioEvent.create(audio1148, 0.000000, 4.000000, "CRASH", "C5", 1.000000));
    // Instrument Audio Chords
    audio1150 = insert(InstrumentAudio.create(instrument38, "Hihat Closed 14", "5ab088d9-7302-48f1-bd57-74f6924e61ee-instrument-38-audio.wav", 0.000000, 0.293000, 121.000000, 620.465000, 0.600000));
    // Instrument Audio Events
    audioEvent1151 = insert(InstrumentAudioEvent.create(audio1150, 0.000000, 0.500000, "HIHAT", "Eb5", 1.000000));
    // Instrument Audio Chords
    audio1152 = insert(InstrumentAudio.create(instrument38, "D", "fb0fe08a-e881-404c-a9dd-532bc7cab45c-instrument-38-audio.wav", 0.000000, 1.837000, 88.000000, 250.568000, 0.600000));
    // Instrument Audio Events
    audioEvent1153 = insert(InstrumentAudioEvent.create(audio1152, 0.000000, 2.000000, "TOM", "B3", 1.000000));
    // Instrument Audio Chords
    audio1154 = insert(InstrumentAudio.create(instrument38, "Taiko", "8aac83e9-16b2-4840-a6e9-a28b9440d817-instrument-38-audio.wav", 0.000000, 2.006000, 88.000000, 2205.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1155 = insert(InstrumentAudioEvent.create(audio1154, 0.000000, 2.000000, "KICK", "Db3", 1.000000));
    // Instrument Audio Chords
    audio1156 = insert(InstrumentAudio.create(instrument38, "Hihat Closed 15", "27ef6f44-99a4-4d57-95cf-448bebb702a2-instrument-38-audio.wav", 0.000000, 0.314000, 121.000000, 205.855000, 0.600000));
    // Instrument Audio Events
    audioEvent1157 = insert(InstrumentAudioEvent.create(audio1156, 0.000000, 0.500000, "HIHAT", "Ab3", 1.000000));
    // Instrument Audio Chords
    audio1158 = insert(InstrumentAudio.create(instrument38, "Shamisen B", "20d70530-d30d-43ef-9b07-478ebfe18687-instrument-38-audio.wav", 0.005000, 1.353000, 88.000000, 264.072000, 0.600000));
    // Instrument Audio Events
    audioEvent1159 = insert(InstrumentAudioEvent.create(audio1158, 0.000000, 1.000000, "CRASH", "c4", 1.000000));
    // Instrument Audio Chords
    audio1160 = insert(InstrumentAudio.create(instrument38, "Shami", "8a5b5710-d5a7-48a9-b1b1-02514aedb0f3-instrument-38-audio.wav", 0.006000, 0.999000, 88.000000, 262.500000, 0.600000));
    // Instrument Audio Events
    audioEvent1161 = insert(InstrumentAudioEvent.create(audio1160, 0.000000, 1.000000, "CRASH", "c4", 1.000000));
    // Instrument Audio Chords
    audio1162 = insert(InstrumentAudio.create(instrument38, "C", "def6bba3-4fa7-4248-b1cf-2fe875734f03-instrument-38-audio.wav", 0.000000, 0.805000, 88.000000, 722.951000, 0.600000));
    // Instrument Audio Events
    audioEvent1163 = insert(InstrumentAudioEvent.create(audio1162, 0.000000, 1.000000, "TOM", "Gb5", 1.000000));
    // Instrument Audio Chords
    audio1164 = insert(InstrumentAudio.create(instrument38, "Snare Rim 3", "6d19fe2d-9873-4706-bde3-8f10af0db404-instrument-38-audio.wav", 0.000000, 0.346000, 120.000000, 1002.270000, 0.600000));
    // Instrument Audio Events
    audioEvent1165 = insert(InstrumentAudioEvent.create(audio1164, 0.000000, 1.000000, "SNARE", "B5", 1.000000));
    // Instrument Audio Chords
    audio1166 = insert(InstrumentAudio.create(instrument38, "Snare Rim 2", "c20a1f4b-058f-49b9-b982-adb2a6aaefac-instrument-38-audio.wav", 0.000000, 0.251000, 120.000000, 1050.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1167 = insert(InstrumentAudioEvent.create(audio1166, 0.000000, 1.000000, "SNARE", "C6", 1.000000));
    // Instrument Audio Chords
    audio1168 = insert(InstrumentAudio.create(instrument38, "E", "108bc006-8cec-4ba8-84da-75c94a77bc19-instrument-38-audio.wav", 0.000000, 0.895000, 88.000000, 588.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1169 = insert(InstrumentAudioEvent.create(audio1168, 0.000000, 1.000000, "TOM", "D5", 1.000000));
    // Instrument Audio Chords
    audio1170 = insert(InstrumentAudio.create(instrument38, "Snare Rim 4", "f2681dcd-8ba3-4baa-a80a-14cad5dbb8f4-instrument-38-audio.wav", 0.000000, 0.216000, 120.000000, 304.138000, 0.600000));
    // Instrument Audio Events
    audioEvent1171 = insert(InstrumentAudioEvent.create(audio1170, 0.000000, 1.000000, "SNARE", "Eb4", 1.000000));
    // Instrument Audio Chords
    audio1172 = insert(InstrumentAudio.create(instrument38, "Kick 86", "be679c0a-93ea-451d-bc10-f454673cbc28-instrument-38-audio.wav", 0.000000, 1.046000, 120.000000, 67.951000, 0.600000));
    // Instrument Audio Events
    audioEvent1173 = insert(InstrumentAudioEvent.create(audio1172, 0.000000, 1.000000, "KICK", "Db2", 1.000000));
    // Instrument Audio Chords
    audio1174 = insert(InstrumentAudio.create(instrument38, "Hihat Open 12", "b7f421ab-381a-4983-b732-658374b8ce33-instrument-38-audio.wav", 0.000000, 0.410000, 121.000000, 155.116000, 0.600000));
    // Instrument Audio Events
    audioEvent1175 = insert(InstrumentAudioEvent.create(audio1174, 0.000000, 0.500000, "HIHATOPEN", "Eb3", 1.000000));
    // Instrument Audio Chords
    audio1176 = insert(InstrumentAudio.create(instrument38, "Kick 87", "3badc739-a69a-4bce-8fad-510aaf0f3074-instrument-38-audio.wav", 0.000000, 1.158000, 120.000000, 64.663000, 0.600000));
    // Instrument Audio Events
    audioEvent1177 = insert(InstrumentAudioEvent.create(audio1176, 0.000000, 1.000000, "KICK", "C2", 1.000000));
    // Instrument Audio Chords
    audio1178 = insert(InstrumentAudio.create(instrument38, "F", "0e95155d-d5a7-4ec4-a4b8-1830a54589cb-instrument-38-audio.wav", 0.000000, 0.802000, 88.000000, 773.684000, 0.600000));
    // Instrument Audio Events
    audioEvent1179 = insert(InstrumentAudioEvent.create(audio1178, 0.000000, 1.000000, "TOM", "G5", 1.000000));
    // Instrument Audio Chords
    audio1180 = insert(InstrumentAudio.create(instrument38, "Snare Rim 1", "34b0ad8c-eb55-4fb7-860e-2b50b5e181b6-instrument-38-audio.wav", 0.000000, 0.342000, 120.000000, 280.892000, 0.600000));
    // Instrument Audio Events
    audioEvent1181 = insert(InstrumentAudioEvent.create(audio1180, 0.000000, 1.000000, "SNARE", "Db4", 1.000000));
    // Instrument Audio Chords
    audio1182 = insert(InstrumentAudio.create(instrument38, "Hihat Open 14", "3cc4f146-ba60-4489-b4c1-310069e1c509-instrument-38-audio.wav", 0.000000, 0.422000, 121.000000, 149.840000, 0.600000));
    // Instrument Audio Events
    audioEvent1183 = insert(InstrumentAudioEvent.create(audio1182, 0.000000, 0.500000, "HIHATOPEN", "D3", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument Wind B (legacy)
    instrument39 = insert(Instrument.create(user1, library1, "Percussive", "Published", "Wind B (legacy)", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument39, "Wind"));
    // Instrument Audios
    audio1184 = insert(InstrumentAudio.create(instrument39, "Snare 7", "d79952fc-c6e3-4318-8b73-23d15449df57-instrument-39-audio.wav", 0.000000, 1.123000, 121.000000, 270.552000, 0.600000));
    // Instrument Audio Events
    audioEvent1185 = insert(InstrumentAudioEvent.create(audio1184, 0.000000, 1.000000, "SNARE", "Db4", 1.000000));
    // Instrument Audio Chords
    audio1186 = insert(InstrumentAudio.create(instrument39, "Tom High D1", "dbbe75be-4db1-4fe9-b2cc-c9f9672cd168-instrument-39-audio.wav", 0.000000, 0.859000, 121.000000, 179.268000, 0.600000));
    // Instrument Audio Events
    audioEvent1187 = insert(InstrumentAudioEvent.create(audio1186, 0.000000, 1.000000, "TOM", "F3", 1.000000));
    // Instrument Audio Chords
    audio1188 = insert(InstrumentAudio.create(instrument39, "Kick 23", "4b94b1e1-c1d1-455d-8d23-3faaf1c89a99-instrument-39-audio.wav", 0.000000, 0.992000, 121.000000, 52.814000, 0.600000));
    // Instrument Audio Events
    audioEvent1189 = insert(InstrumentAudioEvent.create(audio1188, 0.000000, 1.000000, "KICK", "Ab1", 1.000000));
    // Instrument Audio Chords
    audio1190 = insert(InstrumentAudio.create(instrument39, "Kick 38", "7d3f0e48-77b8-4dbd-8f02-813a779b2c67-instrument-39-audio.wav", 0.000000, 0.774000, 121.000000, 58.800000, 0.600000));
    // Instrument Audio Events
    audioEvent1191 = insert(InstrumentAudioEvent.create(audio1190, 0.000000, 1.000000, "KICK", "Bb1", 1.000000));
    // Instrument Audio Chords
    audio1192 = insert(InstrumentAudio.create(instrument39, "Hihat Closed (Shaker) 3", "7965cf0f-4721-4853-b466-208d193b878a-instrument-39-audio.wav", 0.015000, 0.087000, 121.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1193 = insert(InstrumentAudioEvent.create(audio1192, 0.000000, 0.500000, "HIHAT", "G8", 1.000000));
    // Instrument Audio Chords
    audio1194 = insert(InstrumentAudio.create(instrument39, "Hihat Open (Tambourine) 5", "a579d1f0-6339-4f76-a001-6cb329489dfa-instrument-39-audio.wav", 0.000000, 0.262000, 121.000000, 2594.120000, 0.600000));
    // Instrument Audio Events
    audioEvent1195 = insert(InstrumentAudioEvent.create(audio1194, 0.000000, 0.500000, "HIHATOPEN", "E7", 1.000000));
    // Instrument Audio Chords
    audio1196 = insert(InstrumentAudio.create(instrument39, "Hihat Open (Tambourine) 2", "05e5a569-5754-4976-9869-e3188ce046cf-instrument-39-audio.wav", 0.000000, 0.321000, 121.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1197 = insert(InstrumentAudioEvent.create(audio1196, 0.000000, 0.500000, "HIHATOPEN", "G8", 1.000000));
    // Instrument Audio Chords
    audio1198 = insert(InstrumentAudio.create(instrument39, "Kick 32", "bb337e3f-310c-4732-8b4c-8b80dedc9504-instrument-39-audio.wav", 0.000000, 0.753000, 121.000000, 56.394000, 0.600000));
    // Instrument Audio Events
    audioEvent1199 = insert(InstrumentAudioEvent.create(audio1198, 0.000000, 1.000000, "KICK", "A1", 1.000000));
    // Instrument Audio Chords
    audio1200 = insert(InstrumentAudio.create(instrument39, "Tom High A1", "f1798f7d-e93d-47c4-975b-0a81a11dd08a-instrument-39-audio.wav", 0.000000, 1.065000, 121.000000, 164.552000, 0.600000));
    // Instrument Audio Events
    audioEvent1201 = insert(InstrumentAudioEvent.create(audio1200, 0.000000, 1.000000, "TOM", "E3", 1.000000));
    // Instrument Audio Chords
    audio1202 = insert(InstrumentAudio.create(instrument39, "Hihat Closed (Shaker) 5", "a6f581cd-7c96-4726-8b22-4337a66d0022-instrument-39-audio.wav", 0.015000, 0.092000, 121.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1203 = insert(InstrumentAudioEvent.create(audio1202, 0.000000, 0.500000, "HIHAT", "G8", 1.000000));
    // Instrument Audio Chords
    audio1204 = insert(InstrumentAudio.create(instrument39, "Tom Low D2", "83dbc947-ea66-4fbf-9bb5-a86ac0c35a4d-instrument-39-audio.wav", 0.000000, 0.997000, 121.000000, 96.248000, 0.600000));
    // Instrument Audio Events
    audioEvent1205 = insert(InstrumentAudioEvent.create(audio1204, 0.000000, 1.000000, "TOM", "G2", 1.000000));
    // Instrument Audio Chords
    audio1206 = insert(InstrumentAudio.create(instrument39, "Snare 3", "b7402bc0-1ae4-480f-97fb-538de7405d99-instrument-39-audio.wav", 0.000000, 1.057000, 121.000000, 270.552000, 0.600000));
    // Instrument Audio Events
    audioEvent1207 = insert(InstrumentAudioEvent.create(audio1206, 0.000000, 1.000000, "SNARE", "Db44", 1.000000));
    // Instrument Audio Chords
    audio1208 = insert(InstrumentAudio.create(instrument39, "Tom High E1", "f662ab55-da9e-4a7c-8cc9-9f0bdfb85af6-instrument-39-audio.wav", 0.000000, 2.049000, 121.000000, 161.538000, 0.600000));
    // Instrument Audio Events
    audioEvent1209 = insert(InstrumentAudioEvent.create(audio1208, 0.000000, 1.000000, "TOM", "E3", 1.000000));
    // Instrument Audio Chords
    audio1210 = insert(InstrumentAudio.create(instrument39, "Tom Low E4", "e7ff4972-b1d2-4f23-8334-7f38e65251e4-instrument-39-audio.wav", 0.000000, 2.194000, 121.000000, 81.818000, 0.600000));
    // Instrument Audio Events
    audioEvent1211 = insert(InstrumentAudioEvent.create(audio1210, 0.000000, 1.000000, "TOM", "e2", 1.000000));
    // Instrument Audio Chords
    audio1212 = insert(InstrumentAudio.create(instrument39, "Snare 44", "42c87a08-09c0-4186-8b8c-7fa588f335a6-instrument-39-audio.wav", 0.000000, 0.458000, 121.000000, 11025.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1213 = insert(InstrumentAudioEvent.create(audio1212, 0.000000, 1.000000, "SNARE", "f9", 1.000000));
    // Instrument Audio Chords
    audio1214 = insert(InstrumentAudio.create(instrument39, "Kick 16", "88525f80-96f7-4208-833e-e3026dc04a2b-instrument-39-audio.wav", 0.000000, 0.808000, 121.000000, 59.274000, 0.600000));
    // Instrument Audio Events
    audioEvent1215 = insert(InstrumentAudioEvent.create(audio1214, 0.000000, 1.000000, "KICK", "Bb1", 1.000000));
    // Instrument Audio Chords
    audio1216 = insert(InstrumentAudio.create(instrument39, "Hihat Closed (Shaker) 4", "295d712b-0726-48fb-9238-e29f7a6e3ca0-instrument-39-audio.wav", 0.015000, 0.086000, 121.000000, 7350.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1217 = insert(InstrumentAudioEvent.create(audio1216, 0.000000, 0.500000, "HIHAT", "Bb8", 1.000000));
    // Instrument Audio Chords
    audio1218 = insert(InstrumentAudio.create(instrument39, "Tom Low A2", "0c205d69-ae55-4b79-ba04-1b7a37877bab-instrument-39-audio.wav", 0.000000, 1.457000, 121.000000, 111.646000, 0.600000));
    // Instrument Audio Events
    audioEvent1219 = insert(InstrumentAudioEvent.create(audio1218, 0.000000, 1.000000, "TOM", "A2", 1.000000));
    // Instrument Audio Chords
    audio1220 = insert(InstrumentAudio.create(instrument39, "Hihat Open (Tambourine) 4", "68419cfa-0b82-484a-8a2b-a84348b7fb88-instrument-39-audio.wav", 0.000000, 0.148000, 121.000000, 4594.120000, 0.600000));
    // Instrument Audio Events
    audioEvent1221 = insert(InstrumentAudioEvent.create(audio1220, 0.000000, 0.500000, "HIHATOPEN", "e7", 1.000000));
    // Instrument Audio Chords
    audio1222 = insert(InstrumentAudio.create(instrument39, "Hihat Open (Tambourine) 1", "0e1affdb-ba5b-4107-a0d4-4ef6565d0255-instrument-39-audio.wav", 0.000000, 0.297000, 121.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1223 = insert(InstrumentAudioEvent.create(audio1222, 0.000000, 0.500000, "HIHATOPEN", "g8", 1.000000));
    // Instrument Audio Chords
    audio1224 = insert(InstrumentAudio.create(instrument39, "Crash 12", "3cb0cdaa-f60d-4c34-9202-795543bf0982-instrument-39-audio.wav", 0.000000, 1.768000, 121.000000, 4900.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1225 = insert(InstrumentAudioEvent.create(audio1224, 0.000000, 1.000000, "CRASH", "Eb8", 1.000000));
    // Instrument Audio Chords
    audio1226 = insert(InstrumentAudio.create(instrument39, "Hihat Closed (Shaker) 1", "f7715bfc-ac3e-45f7-9e64-b53406d7376d-instrument-39-audio.wav", 0.025000, 0.100000, 121.000000, 2321.050000, 0.600000));
    // Instrument Audio Events
    audioEvent1227 = insert(InstrumentAudioEvent.create(audio1226, 0.000000, 0.500000, "HIHAT", "G8", 1.000000));
    // Instrument Audio Chords

  }

  private void go9() throws Exception {
    // Insert Percussive-type Instrument Wind Large
    instrument40 = insert(Instrument.create(user3, library3, "Percussive", "Published", "Wind Large", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument40, "Wind"));
    insert(InstrumentMeme.create(instrument40, "Large"));
    // Instrument Audios
    audio1228 = insert(InstrumentAudio.create(instrument40, "Tiny Vibrating String", "5a34d08c-ae2a-4d41-be0c-599e4f641a48-instrument-40-audio.wav", 0.000000, 0.395000, 121.000000, 716.418000, 0.600000));
    // Instrument Audio Events
    audioEvent1229 = insert(InstrumentAudioEvent.create(audio1228, 0.000000, 1.000000, "TOM", "F5", 1.000000));
    // Instrument Audio Chords
    audio1230 = insert(InstrumentAudio.create(instrument40, "Snare 2", "28d1308d-ac22-4469-af63-616b39a55f7e-instrument-40-audio.wav", 0.000000, 0.181000, 121.000000, 156.863000, 0.600000));
    // Instrument Audio Events
    audioEvent1231 = insert(InstrumentAudioEvent.create(audio1230, 0.000000, 1.000000, "SNARE", "Eb3", 1.000000));
    // Instrument Audio Chords
    audio1232 = insert(InstrumentAudio.create(instrument40, "Berimbau-ish Hi-Hat", "f71746de-a0c8-4c3a-9284-36dd7067a0b1-instrument-40-audio.wav", 0.000000, 0.209000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent1233 = insert(InstrumentAudioEvent.create(audio1232, 0.000000, 1.000000, "HIHATOPEN", "A8", 1.000000));
    // Instrument Audio Chords
    audio1234 = insert(InstrumentAudio.create(instrument40, "Slammed Phasey Hat 2", "6154f36a-b18f-4282-89a8-dd0fbb0adfec-instrument-40-audio.wav", 0.000000, 0.375000, 121.000000, 2666.670000, 0.600000));
    // Instrument Audio Events
    audioEvent1235 = insert(InstrumentAudioEvent.create(audio1234, 0.000000, 1.000000, "HIHATOPEN", "E6", 1.000000));
    // Instrument Audio Chords
    audio1236 = insert(InstrumentAudio.create(instrument40, "Clapping Snare Spread", "b45e5925-3c6e-4423-a278-e1fb8c52579e-instrument-40-audio.wav", 0.000000, 0.105000, 121.000000, 641.176000, 0.600000));
    // Instrument Audio Events
    audioEvent1237 = insert(InstrumentAudioEvent.create(audio1236, 0.000000, 1.000000, "SNARE", "A#5", 1.000000));
    // Instrument Audio Chords
    audio1238 = insert(InstrumentAudio.create(instrument40, "Kick 2", "649f1db8-ff49-40bb-83f0-29b5bd880c6d-instrument-40-audio.wav", 0.000000, 0.345000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1239 = insert(InstrumentAudioEvent.create(audio1238, 0.000000, 1.000000, "KICK", "F#8", 1.000000));
    // Instrument Audio Chords
    audio1240 = insert(InstrumentAudio.create(instrument40, "Fluxing Shaker 1", "a55df999-37fa-414a-9fe2-4db2098ae7de-instrument-40-audio.wav", 0.000000, 0.306000, 121.000000, 1411.770000, 0.600000));
    // Instrument Audio Events
    audioEvent1241 = insert(InstrumentAudioEvent.create(audio1240, 0.000000, 1.000000, "TOM", "F6", 1.000000));
    // Instrument Audio Chords
    audio1242 = insert(InstrumentAudio.create(instrument40, "Snare 5", "96ebdcac-f6d0-4aad-8b45-293c6c182a07-instrument-40-audio.wav", 0.000000, 0.236000, 121.000000, 5333.330000, 0.600000));
    // Instrument Audio Events
    audioEvent1243 = insert(InstrumentAudioEvent.create(audio1242, 0.000000, 1.000000, "SNARE", "E8", 1.000000));
    // Instrument Audio Chords
    audio1244 = insert(InstrumentAudio.create(instrument40, "Snare 6", "64703edd-ae19-4ffb-84f3-d016d1209fc0-instrument-40-audio.wav", 0.000000, 0.245000, 121.000000, 5333.330000, 0.600000));
    // Instrument Audio Events
    audioEvent1245 = insert(InstrumentAudioEvent.create(audio1244, 0.000000, 1.000000, "SNARE", "E8", 1.000000));
    // Instrument Audio Chords
    audio1246 = insert(InstrumentAudio.create(instrument40, "Dirty Sweep", "c1da2d42-8504-4eda-95e9-95da8df53cc4-instrument-40-audio.wav", 0.000000, 0.338000, 121.000000, 207.792000, 0.600000));
    // Instrument Audio Events
    audioEvent1247 = insert(InstrumentAudioEvent.create(audio1246, 0.000000, 1.000000, "TOM", "G#3", 1.000000));
    // Instrument Audio Chords
    audio1248 = insert(InstrumentAudio.create(instrument40, "Crash 19", "7aac02ba-5ab8-4471-845b-22f250b96e88-instrument-40-audio.wav", 0.046000, 2.887000, 121.000000, 4900.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1249 = insert(InstrumentAudioEvent.create(audio1248, 0.000000, 4.000000, "CRASH", "D#8", 1.000000));
    // Instrument Audio Chords
    audio1250 = insert(InstrumentAudio.create(instrument40, "Crash 12", "95534bbd-dcd1-4fcc-afc8-429ed3298e61-instrument-40-audio.wav", 0.035000, 5.605000, 121.000000, 4009.090000, 0.600000));
    // Instrument Audio Events
    audioEvent1251 = insert(InstrumentAudioEvent.create(audio1250, 0.000000, 4.000000, "CRASH", "B7", 1.000000));
    // Instrument Audio Chords
    audio1252 = insert(InstrumentAudio.create(instrument40, "Snare 1", "618972fa-818c-49ab-87c8-f8c73386917a-instrument-40-audio.wav", 0.000000, 0.134000, 121.000000, 2666.670000, 0.600000));
    // Instrument Audio Events
    audioEvent1253 = insert(InstrumentAudioEvent.create(audio1252, 0.000000, 1.000000, "SNARE", "E7", 1.000000));
    // Instrument Audio Chords
    audio1254 = insert(InstrumentAudio.create(instrument40, "Reverse Life 1 second", "43675ee8-ccbc-476f-ae00-8a9f42fab6bb-instrument-40-audio.wav", 0.020000, 0.283000, 121.000000, 923.077000, 0.600000));
    // Instrument Audio Events
    audioEvent1255 = insert(InstrumentAudioEvent.create(audio1254, 0.000000, 1.000000, "TOM", "A#5", 1.000000));
    // Instrument Audio Chords
    audio1256 = insert(InstrumentAudio.create(instrument40, "Slammed Phasey Hat 1", "821e6767-6123-440e-b785-d05de10d9ee6-instrument-40-audio.wav", 0.000000, 0.375000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1257 = insert(InstrumentAudioEvent.create(audio1256, 0.000000, 1.000000, "HIHATOPEN", "F#8", 1.000000));
    // Instrument Audio Chords
    audio1258 = insert(InstrumentAudio.create(instrument40, "Kick 4", "96f9a747-3ec3-4b58-8a90-8fc7bd45ecba-instrument-40-audio.wav", 0.000000, 0.308000, 121.000000, 76.800000, 0.600000));
    // Instrument Audio Events
    audioEvent1259 = insert(InstrumentAudioEvent.create(audio1258, 0.000000, 1.000000, "KICK", "D#2", 1.000000));
    // Instrument Audio Chords
    audio1260 = insert(InstrumentAudio.create(instrument40, "Clapping Snare Spread 2", "58283ebc-53e3-4f33-b01d-57f8c5541e2f-instrument-40-audio.wav", 0.000000, 0.206000, 121.000000, 237.624000, 0.600000));
    // Instrument Audio Events
    audioEvent1261 = insert(InstrumentAudioEvent.create(audio1260, 0.000000, 1.000000, "SNARE", "A#3", 1.000000));
    // Instrument Audio Chords
    audio1262 = insert(InstrumentAudio.create(instrument40, "Kick 3", "bda7865e-6158-4fcb-ab76-33c84b15eae3-instrument-40-audio.wav", 0.000000, 0.360000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1263 = insert(InstrumentAudioEvent.create(audio1262, 0.000000, 1.000000, "KICK", "B8", 1.000000));
    // Instrument Audio Chords
    audio1264 = insert(InstrumentAudio.create(instrument40, "Kick 1", "73379128-dfca-4eeb-b8b4-7c111e5f38c4-instrument-40-audio.wav", 0.000000, 0.147000, 121.000000, 9600.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1265 = insert(InstrumentAudioEvent.create(audio1264, 0.000000, 1.000000, "KICK", "D9", 1.000000));
    // Instrument Audio Chords
    audio1266 = insert(InstrumentAudio.create(instrument40, "Vibraslap", "1bf9238f-dbf4-4e54-b18e-1a5eee2eaeb4-instrument-40-audio.wav", 0.000000, 1.159000, 121.000000, 2666.670000, 0.600000));
    // Instrument Audio Events
    audioEvent1267 = insert(InstrumentAudioEvent.create(audio1266, 0.000000, 1.000000, "CRASH", "E7", 1.000000));
    // Instrument Audio Chords
    audio1268 = insert(InstrumentAudio.create(instrument40, "Mid Tom 3", "932664e1-2a18-4816-8509-ed8a4dde5161-instrument-40-audio.wav", 0.000000, 0.950000, 121.000000, 5333.330000, 0.600000));
    // Instrument Audio Events
    audioEvent1269 = insert(InstrumentAudioEvent.create(audio1268, 0.000000, 1.000000, "TOM", "E8", 1.000000));
    // Instrument Audio Chords
    audio1270 = insert(InstrumentAudio.create(instrument40, "Short Lazer", "a53a0ec5-ce7e-42fc-b014-d83f1460cad4-instrument-40-audio.wav", 0.000000, 0.197000, 121.000000, 3200.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1271 = insert(InstrumentAudioEvent.create(audio1270, 0.000000, 1.000000, "TOM", "G7", 1.000000));
    // Instrument Audio Chords
    audio1272 = insert(InstrumentAudio.create(instrument40, "Hi-Hat 1", "855645f7-1c37-4852-820b-e65b9eb2beed-instrument-40-audio.wav", 0.000000, 0.068000, 121.000000, 16000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1273 = insert(InstrumentAudioEvent.create(audio1272, 0.000000, 1.000000, "HIHAT", "B9", 1.000000));
    // Instrument Audio Chords
    audio1274 = insert(InstrumentAudio.create(instrument40, "Crashing Cabinet", "459f065f-a3aa-4190-8f0e-ffed0cbae468-instrument-40-audio.wav", 0.006000, 1.358000, 121.000000, 2000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1275 = insert(InstrumentAudioEvent.create(audio1274, 0.000000, 1.000000, "CRASH", "B6", 1.000000));
    // Instrument Audio Chords
    audio1276 = insert(InstrumentAudio.create(instrument40, "Snare 4", "644ff7da-2674-4644-9dab-f65044b1e662-instrument-40-audio.wav", 0.000000, 0.122000, 121.000000, 224.299000, 0.600000));
    // Instrument Audio Events
    audioEvent1277 = insert(InstrumentAudioEvent.create(audio1276, 0.000000, 1.000000, "SNARE", "A3", 1.000000));
    // Instrument Audio Chords
    audio1278 = insert(InstrumentAudio.create(instrument40, "Peering Insect", "ede6468f-2ecf-44e3-9ce9-6749f54e4b23-instrument-40-audio.wav", 0.000000, 0.116000, 121.000000, 259.459000, 0.600000));
    // Instrument Audio Events
    audioEvent1279 = insert(InstrumentAudioEvent.create(audio1278, 0.000000, 1.000000, "TOM", "C4", 1.000000));
    // Instrument Audio Chords
    audio1280 = insert(InstrumentAudio.create(instrument40, "Hi Tom", "6339d702-184d-449d-8a2a-9422c1c247f1-instrument-40-audio.wav", 0.000000, 1.286000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1281 = insert(InstrumentAudioEvent.create(audio1280, 0.000000, 1.000000, "TOM", "F#8", 1.000000));
    // Instrument Audio Chords
    audio1282 = insert(InstrumentAudio.create(instrument40, "Dead Clave", "bfcdb30f-664a-4c2e-ab75-9ba5cf2f703e-instrument-40-audio.wav", 0.000000, 0.015000, 121.000000, 421.053000, 0.600000));
    // Instrument Audio Events
    audioEvent1283 = insert(InstrumentAudioEvent.create(audio1282, 0.000000, 1.000000, "TOM", "Ab4", 1.000000));
    // Instrument Audio Chords
    audio1284 = insert(InstrumentAudio.create(instrument40, "Hi-Hat Open", "dc725dea-5c27-4f16-aba4-a9ba84ed313b-instrument-27-audio.wav", 0.000000, 0.500000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1285 = insert(InstrumentAudioEvent.create(audio1284, 0.000000, 1.000000, "HIHATOPEN", "X", 1.000000));
    // Instrument Audio Chords
    audio1286 = insert(InstrumentAudio.create(instrument40, "Mid Tom", "ae0e0456-e47e-4d02-9726-5e9736952564-instrument-40-audio.wav", 0.000000, 1.286000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1287 = insert(InstrumentAudioEvent.create(audio1286, 0.000000, 1.000000, "TOM", "F#8", 1.000000));
    // Instrument Audio Chords
    audio1288 = insert(InstrumentAudio.create(instrument40, "Rim Click 1", "5ea3562c-3058-4062-862d-29171a1a1cea-instrument-40-audio.wav", 0.000000, 0.124000, 121.000000, 923.077000, 0.600000));
    // Instrument Audio Events
    audioEvent1289 = insert(InstrumentAudioEvent.create(audio1288, 0.000000, 1.000000, "SNARE", "Bb5", 1.000000));
    // Instrument Audio Chords
    audio1290 = insert(InstrumentAudio.create(instrument40, "Phasing Triangle", "e34f439c-5f18-4841-943f-5264b56f90bc-instrument-40-audio.wav", 0.000000, 1.242000, 121.000000, 369.231000, 0.600000));
    // Instrument Audio Events
    audioEvent1291 = insert(InstrumentAudioEvent.create(audio1290, 0.000000, 1.000000, "CRASH", "F#4", 1.000000));
    // Instrument Audio Chords
    audio1292 = insert(InstrumentAudio.create(instrument40, "Snare 3", "98899718-561c-41cc-b783-7ffccd081ed9-instrument-40-audio.wav", 0.005000, 0.256000, 121.000000, 241.206000, 0.600000));
    // Instrument Audio Events
    audioEvent1293 = insert(InstrumentAudioEvent.create(audio1292, 0.000000, 1.000000, "SNARE", "B3", 1.000000));
    // Instrument Audio Chords
    audio1294 = insert(InstrumentAudio.create(instrument40, "Fluxing Shaker 2", "3a4c6376-c4f6-4538-846e-2c6d32b1a3f0-instrument-40-audio.wav", 0.000000, 0.363000, 121.000000, 1263.160000, 0.600000));
    // Instrument Audio Events
    audioEvent1295 = insert(InstrumentAudioEvent.create(audio1294, 0.000000, 1.000000, "TOM", "D#6", 1.000000));
    // Instrument Audio Chords
    audio1296 = insert(InstrumentAudio.create(instrument40, "Electric Wind Sweep", "79e64c9a-2163-4eb0-b4c7-16ab5ad821cd-instrument-40-audio.wav", 0.200000, 0.937000, 121.000000, 1000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1297 = insert(InstrumentAudioEvent.create(audio1296, 0.000000, 1.000000, "CRASH", "B5", 1.000000));
    // Instrument Audio Chords
    audio1298 = insert(InstrumentAudio.create(instrument40, "Clap 1", "03b7667b-4b3f-4649-9671-e6fae61e18f0-instrument-40-audio.wav", 0.000000, 0.385000, 121.000000, 6857.140000, 0.600000));
    // Instrument Audio Events
    audioEvent1299 = insert(InstrumentAudioEvent.create(audio1298, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio1300 = insert(InstrumentAudio.create(instrument40, "Snare 7", "95f6d747-cc59-4376-a03f-f15de2d6771d-instrument-40-audio.wav", 0.000000, 0.407000, 121.000000, 480.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1301 = insert(InstrumentAudioEvent.create(audio1300, 0.000000, 1.000000, "SNARE", "B4", 1.000000));
    // Instrument Audio Chords
    audio1302 = insert(InstrumentAudio.create(instrument40, "Mid Tom 2", "15e73c79-697f-4ac8-bc43-a313f4717714-instrument-40-audio.wav", 0.000000, 0.789000, 121.000000, 6000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1303 = insert(InstrumentAudioEvent.create(audio1302, 0.000000, 1.000000, "TOM", "F#8", 1.000000));
    // Instrument Audio Chords


    // Insert Percussive-type Instrument Wind Small
    instrument30 = insert(Instrument.create(user3, library3, "Percussive", "Published", "Wind Small", 0.600000));
    // Instrument Memes
    insert(InstrumentMeme.create(instrument30, "Wind"));
    insert(InstrumentMeme.create(instrument30, "Small"));
    // Instrument Audios
    audio1304 = insert(InstrumentAudio.create(instrument30, "Tabla", "f34dfe75-5239-45d8-9b06-d1b6e4240199-instrument-30-audio.wav", 0.000200, 0.594000, 121.000000, 77.544000, 0.600000));
    // Instrument Audio Events
    audioEvent1305 = insert(InstrumentAudioEvent.create(audio1304, 0.000000, 1.000000, "TOM", "Eb2", 1.000000));
    // Instrument Audio Chords
    audio1306 = insert(InstrumentAudio.create(instrument30, "Kick with Heavy Attack and Heavy Sub", "3d21fbf5-94fd-4f24-853a-f449bff74d6a-instrument-30-audio.wav", 0.000100, 0.875000, 121.000000, 103.448000, 0.600000));
    // Instrument Audio Events
    audioEvent1307 = insert(InstrumentAudioEvent.create(audio1306, 0.000000, 1.000000, "KICK", "Ab2", 1.000000));
    // Instrument Audio Chords
    audio1308 = insert(InstrumentAudio.create(instrument30, "Basketball-like Snare", "446caeea-a77e-47d8-a69f-1a44ec1f4f74-instrument-30-audio.wav", 0.000200, 0.250000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1309 = insert(InstrumentAudioEvent.create(audio1308, 0.000000, 1.000000, "SNARE", "D8", 1.000000));
    // Instrument Audio Chords
    audio1310 = insert(InstrumentAudio.create(instrument30, "Undulating Low Tom/Kick", "c23128ff-fe96-4e9b-b356-f1b3cb951a2d-instrument-30-audio.wav", 0.000000, 0.625000, 121.000000, 269.663000, 0.600000));
    // Instrument Audio Events
    audioEvent1311 = insert(InstrumentAudioEvent.create(audio1310, 0.000000, 1.000000, "KICK", "Db4", 1.000000));
    // Instrument Audio Chords
    audio1312 = insert(InstrumentAudio.create(instrument30, "Knocky Muted Tom", "95d5369a-71aa-4b2f-be0e-c72044091307-instrument-30-audio.wav", 0.000600, 0.219000, 121.000000, 193.548000, 0.600000));
    // Instrument Audio Events
    audioEvent1313 = insert(InstrumentAudioEvent.create(audio1312, 0.000000, 1.000000, "TOM", "X", 1.000000));
    // Instrument Audio Chords
    audio1314 = insert(InstrumentAudio.create(instrument30, "Percussive Flam", "9bd6fafa-0363-45d5-a17d-05ee8a40095b-instrument-30-audio.wav", 0.007500, 0.281000, 121.000000, 1263.160000, 0.600000));
    // Instrument Audio Events
    audioEvent1315 = insert(InstrumentAudioEvent.create(audio1314, 0.000000, 1.000000, "TOM", "Eb6", 1.000000));
    // Instrument Audio Chords
    audio1316 = insert(InstrumentAudio.create(instrument30, "Dead Studio Snare", "0c460941-c090-4f71-9ff5-a632319cb5c9-instrument-30-audio.wav", 0.000000, 0.250000, 121.000000, 186.047000, 0.600000));
    // Instrument Audio Events
    audioEvent1317 = insert(InstrumentAudioEvent.create(audio1316, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio1318 = insert(InstrumentAudio.create(instrument30, "Tight Dead Snare 2", "5ff14782-bf9b-4f0d-8529-bba612872b1d-instrument-30-audio.wav", 0.000300, 0.250000, 121.000000, 84.956000, 0.600000));
    // Instrument Audio Events
    audioEvent1319 = insert(InstrumentAudioEvent.create(audio1318, 0.000000, 1.000000, "SNARE", "F2", 1.000000));
    // Instrument Audio Chords
    audio1320 = insert(InstrumentAudio.create(instrument30, "Electronic Tom w/ Slapback", "c9221c0a-7748-4d54-ba67-6edf709fd42b-instrument-30-audio.wav", 0.001000, 0.875000, 121.000000, 3428.570000, 0.600000));
    // Instrument Audio Events
    audioEvent1321 = insert(InstrumentAudioEvent.create(audio1320, 0.000000, 1.000000, "TOM", "A7", 1.000000));
    // Instrument Audio Chords
    audio1322 = insert(InstrumentAudio.create(instrument30, "Electronic Tom w/ Slapback 2", "a2aa200b-5f88-47f8-a9f4-7ff5e12156d0-instrument-30-audio.wav", 0.009000, 0.875000, 121.000000, 3428.570000, 0.600000));
    // Instrument Audio Events
    audioEvent1323 = insert(InstrumentAudioEvent.create(audio1322, 0.000000, 1.000000, "TOM", "A7", 1.000000));
    // Instrument Audio Chords
    audio1324 = insert(InstrumentAudio.create(instrument30, "Hi-Hat 2", "f19c9101-1ad1-43c9-8fc0-8a18fe116e64-instrument-40-audio.wav", 0.000000, 0.175000, 121.000000, 16000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1325 = insert(InstrumentAudioEvent.create(audio1324, 0.000000, 1.000000, "HIHATOPEN", "B9", 1.000000));
    // Instrument Audio Chords
    audio1326 = insert(InstrumentAudio.create(instrument30, "Metallic Snare", "417cfa8c-f708-44e7-b06f-497dcc56d4a6-instrument-30-audio.wav", 0.000100, 0.250000, 121.000000, 322.148000, 0.600000));
    // Instrument Audio Events
    audioEvent1327 = insert(InstrumentAudioEvent.create(audio1326, 0.000000, 1.000000, "SNARE", "E4", 1.000000));
    // Instrument Audio Chords
    audio1328 = insert(InstrumentAudio.create(instrument30, "Clap-like Percussion", "9acfb60f-9112-4a6c-9697-f5270583c81b-instrument-30-audio.wav", 0.000100, 0.250000, 121.000000, 8000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1329 = insert(InstrumentAudioEvent.create(audio1328, 0.000000, 1.000000, "SNARE", "B8", 1.000000));
    // Instrument Audio Chords
    audio1330 = insert(InstrumentAudio.create(instrument30, "Digital Percussive Flam", "fecb3546-cd26-4ffe-a226-6822a653b9de-instrument-30-audio.wav", 0.000400, 0.250000, 121.000000, 226.415000, 0.600000));
    // Instrument Audio Events
    audioEvent1331 = insert(InstrumentAudioEvent.create(audio1330, 0.000000, 1.000000, "TOM", "A3", 1.000000));
    // Instrument Audio Chords
    audio1332 = insert(InstrumentAudio.create(instrument30, "Hi-Hat 1", "9a91b6f0-1a14-48c7-933b-5a53c7824e3d-instrument-40-audio.wav", 0.000000, 0.068000, 121.000000, 16000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1333 = insert(InstrumentAudioEvent.create(audio1332, 0.000000, 1.000000, "HIHAT", "B9", 1.000000));
    // Instrument Audio Chords
    audio1334 = insert(InstrumentAudio.create(instrument30, "Dubbed Out Clave", "20e27f8a-914f-4091-a2df-769069497ae8-instrument-30-audio.wav", 0.000000, 6.215000, 121.000000, 1777.780000, 0.600000));
    // Instrument Audio Events
    audioEvent1335 = insert(InstrumentAudioEvent.create(audio1334, 0.000000, 1.000000, "TOM", "A6", 1.000000));
    // Instrument Audio Chords
    audio1336 = insert(InstrumentAudio.create(instrument30, "Hi-Hat 1", "adb40492-919f-46ff-808f-83465a6fb5d6-instrument-40-audio.wav", 0.000000, 0.068000, 121.000000, 16000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1337 = insert(InstrumentAudioEvent.create(audio1336, 0.000000, 1.000000, "HIHAT", "B9", 1.000000));
    // Instrument Audio Chords
    audio1338 = insert(InstrumentAudio.create(instrument30, "Future Shaker", "2c0665c5-2a39-4cd1-8c06-2829d81e94f9-instrument-40-audio.wav", 0.000000, 0.257000, 121.000000, 12000.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1339 = insert(InstrumentAudioEvent.create(audio1338, 0.000000, 1.000000, "HIHAT", "F#9", 1.000000));
    // Instrument Audio Chords
    audio1340 = insert(InstrumentAudio.create(instrument30, "Dubby Fog Horn", "26532e58-f9d9-4e99-a916-0d10f83ad9a9-instrument-30-audio.wav", 0.108300, 9.250000, 121.000000, 121.212000, 0.600000));
    // Instrument Audio Events
    audioEvent1341 = insert(InstrumentAudioEvent.create(audio1340, 0.000000, 1.000000, "CRASH", "B2", 1.000000));
    // Instrument Audio Chords
    audio1342 = insert(InstrumentAudio.create(instrument30, "Electronic Small Snare", "b94c3696-ca2a-4d61-b6fe-7108908f4074-instrument-30-audio.wav", 0.000000, 0.344000, 121.000000, 4800.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1343 = insert(InstrumentAudioEvent.create(audio1342, 0.000000, 1.000000, "SNARE", "D8", 1.000000));
    // Instrument Audio Chords
    audio1344 = insert(InstrumentAudio.create(instrument30, "Tight Dead Acoustic Snare", "ef02250e-6420-45dd-92f2-21e736df1545-instrument-30-audio.wav", 0.000300, 0.250000, 121.000000, 428.571000, 0.600000));
    // Instrument Audio Events
    audioEvent1345 = insert(InstrumentAudioEvent.create(audio1344, 0.000000, 1.000000, "SNARE", "X", 1.000000));
    // Instrument Audio Chords
    audio1346 = insert(InstrumentAudio.create(instrument30, "White Noise Crash", "07812c0f-d7f6-425b-8964-53cafabaee53-instrument-30-audio.wav", 0.000000, 0.594000, 121.000000, 1200.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1347 = insert(InstrumentAudioEvent.create(audio1346, 0.000000, 1.000000, "CRASH", "D6", 1.000000));
    // Instrument Audio Chords
    audio1348 = insert(InstrumentAudio.create(instrument30, "Crash 4", "97e39c49-3121-421f-b344-63d6825a90d4-instrument-30-audio.wav", 0.016000, 3.003000, 121.000000, 1297.060000, 0.600000));
    // Instrument Audio Events
    audioEvent1349 = insert(InstrumentAudioEvent.create(audio1348, 0.000000, 4.000000, "CRASH", "E6", 1.000000));
    // Instrument Audio Chords
    audio1350 = insert(InstrumentAudio.create(instrument30, "Crash 15", "0dfc7cb2-3551-44f3-aaca-9942c11e1b69-instrument-30-audio.wav", 0.008000, 0.998000, 121.000000, 6300.000000, 0.600000));
    // Instrument Audio Events
    audioEvent1351 = insert(InstrumentAudioEvent.create(audio1350, 0.000000, 4.000000, "CRASH", "g8", 1.000000));
    // Instrument Audio Chords


    // Insert Macro-type Program Deep, from Cool to Hot
    program8 = insert(Program.create(user1, library1, "Macro", "Published", "Deep, from Cool to Hot", "G minor", 130.000000, 0.000000));
    // Program Memes
    insert(ProgramMeme.create(program8, "Deep"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1352 = insert(ProgramSequence.create(program8, 0, "from Cool", 0.500000, "G minor", 130.000000));
    sequence1353 = insert(ProgramSequence.create(program8, 0, "to Hot", 0.700000, "C", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1354 = insert(ProgramSequenceBinding.create(sequence1353, 1));
    sequenceBinding1355 = insert(ProgramSequenceBinding.create(sequence1352, 0));
    // Program Sequence Binding Memes
    sequenceBindingMeme1356 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1354, "Hot"));
    sequenceBindingMeme1357 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1355, "Cool"));
    sequenceBindingMeme1358 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1354, "Easy"));
    sequenceBindingMeme1359 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1355, "Hard"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Deep, from Hot to Cool
    program7 = insert(Program.create(user1, library1, "Macro", "Published", "Deep, from Hot to Cool", "C", 130.000000, 0.000000));
    // Program Memes
    insert(ProgramMeme.create(program7, "Deep"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1360 = insert(ProgramSequence.create(program7, 0, "to Cool", 0.500000, "Bb Minor", 130.000000));
    sequence1361 = insert(ProgramSequence.create(program7, 0, "from Hot", 0.700000, "C", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1362 = insert(ProgramSequenceBinding.create(sequence1361, 0));
    sequenceBinding1363 = insert(ProgramSequenceBinding.create(sequence1360, 1));
    // Program Sequence Binding Memes
    sequenceBindingMeme1364 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1362, "Hot"));
    sequenceBindingMeme1365 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1362, "Tropical"));
    sequenceBindingMeme1366 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1363, "Cool"));
    sequenceBindingMeme1367 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1363, "Electro"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Earth to Fire
    program12 = insert(Program.create(user1, library3, "Macro", "Published", "Earth to Fire", "Ebm", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1368 = insert(ProgramSequence.create(program12, 0, "Passion Volcano", 0.600000, "Ebm", 130.000000));
    sequence1369 = insert(ProgramSequence.create(program12, 0, "Exploding", 0.600000, "B", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1370 = insert(ProgramSequenceBinding.create(sequence1369, 1));
    sequenceBinding1371 = insert(ProgramSequenceBinding.create(sequence1368, 0));
    sequenceBinding1372 = insert(ProgramSequenceBinding.create(sequence1368, 2));
    // Program Sequence Binding Memes
    sequenceBindingMeme1373 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1371, "Earth"));
    sequenceBindingMeme1374 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1370, "Fire"));
    sequenceBindingMeme1375 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1372, "Fire"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events

  }

  private void go10() throws Exception {
    // Insert Macro-type Program Earth to Water
    program13 = insert(Program.create(user1, library3, "Macro", "Published", "Earth to Water", "Gm", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1376 = insert(ProgramSequence.create(program13, 0, "Arrival", 0.600000, "F", 130.000000));
    sequence1377 = insert(ProgramSequence.create(program13, 0, "Castaway", 0.600000, "C minor", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1378 = insert(ProgramSequenceBinding.create(sequence1377, 0));
    sequenceBinding1379 = insert(ProgramSequenceBinding.create(sequence1376, 1));
    // Program Sequence Binding Memes
    sequenceBindingMeme1380 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1378, "Earth"));
    sequenceBindingMeme1381 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1379, "Water"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Earth to Wind
    program14 = insert(Program.create(user1, library3, "Macro", "Published", "Earth to Wind", "Cm", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1382 = insert(ProgramSequence.create(program14, 0, "Air", 0.600000, "D", 130.000000));
    sequence1383 = insert(ProgramSequence.create(program14, 0, "Ground", 0.600000, "C minor", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1384 = insert(ProgramSequenceBinding.create(sequence1383, 0));
    sequenceBinding1385 = insert(ProgramSequenceBinding.create(sequence1382, 1));
    // Program Sequence Binding Memes
    sequenceBindingMeme1386 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1384, "Earth"));
    sequenceBindingMeme1387 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1385, "Wind"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Fire to Earth
    program15 = insert(Program.create(user1, library3, "Macro", "Published", "Fire to Earth", "G", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1388 = insert(ProgramSequence.create(program15, 0, "Aspiration", 0.600000, "G", 130.000000));
    sequence1389 = insert(ProgramSequence.create(program15, 0, "Defeat", 0.600000, "Am", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1390 = insert(ProgramSequenceBinding.create(sequence1389, 1));
    sequenceBinding1391 = insert(ProgramSequenceBinding.create(sequence1388, 0));
    // Program Sequence Binding Memes
    sequenceBindingMeme1392 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1390, "Earth"));
    sequenceBindingMeme1393 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1391, "Fire"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Fire to Water
    program16 = insert(Program.create(user1, library3, "Macro", "Published", "Fire to Water", "E", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1394 = insert(ProgramSequence.create(program16, 0, "Glory", 0.600000, "F", 130.000000));
    sequence1395 = insert(ProgramSequence.create(program16, 0, "Volcanic Island", 0.600000, "E", 130.000000));
    sequence1396 = insert(ProgramSequence.create(program16, 0, "Sex on the Beach", 0.600000, "Am", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1397 = insert(ProgramSequenceBinding.create(sequence1394, 2));
    sequenceBinding1398 = insert(ProgramSequenceBinding.create(sequence1395, 0));
    sequenceBinding1399 = insert(ProgramSequenceBinding.create(sequence1396, 1));
    // Program Sequence Binding Memes
    sequenceBindingMeme1400 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1399, "Fire"));
    sequenceBindingMeme1401 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1397, "Water"));
    sequenceBindingMeme1402 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1398, "Fire"));
    sequenceBindingMeme1403 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1399, "Water"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Fire to Wind
    program17 = insert(Program.create(user1, library3, "Macro", "Published", "Fire to Wind", "G", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1404 = insert(ProgramSequence.create(program17, 0, "Dreams", 0.600000, "E", 130.000000));
    sequence1405 = insert(ProgramSequence.create(program17, 0, "Smoke in the Air", 0.600000, "G", 130.000000));
    sequence1406 = insert(ProgramSequence.create(program17, 0, "Waking", 0.600000, "Am", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1407 = insert(ProgramSequenceBinding.create(sequence1406, 2));
    sequenceBinding1408 = insert(ProgramSequenceBinding.create(sequence1404, 1));
    sequenceBinding1409 = insert(ProgramSequenceBinding.create(sequence1405, 0));
    // Program Sequence Binding Memes
    sequenceBindingMeme1410 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1409, "Fire"));
    sequenceBindingMeme1411 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1408, "Fire"));
    sequenceBindingMeme1412 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1408, "Wind"));
    sequenceBindingMeme1413 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1407, "Wind"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Water to Earth
    program32 = insert(Program.create(user1, library3, "Macro", "Published", "Water to Earth", "G", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1414 = insert(ProgramSequence.create(program32, 0, "Irrigation", 0.600000, "G", 130.000000));
    sequence1415 = insert(ProgramSequence.create(program32, 0, "Growth", 0.600000, "Am", 130.000000));
    sequence1416 = insert(ProgramSequence.create(program32, 0, "Nourishment", 0.600000, "C", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1417 = insert(ProgramSequenceBinding.create(sequence1414, 0));
    sequenceBinding1418 = insert(ProgramSequenceBinding.create(sequence1416, 1));
    sequenceBinding1419 = insert(ProgramSequenceBinding.create(sequence1415, 2));
    // Program Sequence Binding Memes
    sequenceBindingMeme1420 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1418, "Earth"));
    sequenceBindingMeme1421 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1419, "Earth"));
    sequenceBindingMeme1422 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1417, "Water"));
    sequenceBindingMeme1423 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1418, "Water"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Water to Fire
    program31 = insert(Program.create(user1, library3, "Macro", "Published", "Water to Fire", "C", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1424 = insert(ProgramSequence.create(program31, 0, "Engine", 0.600000, "Dm", 130.000000));
    sequence1425 = insert(ProgramSequence.create(program31, 0, "Hydrant", 0.600000, "C", 130.000000));
    sequence1426 = insert(ProgramSequence.create(program31, 0, "Steam", 0.600000, "C", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1427 = insert(ProgramSequenceBinding.create(sequence1424, 1));
    sequenceBinding1428 = insert(ProgramSequenceBinding.create(sequence1426, 2));
    sequenceBinding1429 = insert(ProgramSequenceBinding.create(sequence1425, 0));
    // Program Sequence Binding Memes
    sequenceBindingMeme1430 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1427, "Fire"));
    sequenceBindingMeme1431 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1429, "Water"));
    sequenceBindingMeme1432 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1427, "Water"));
    sequenceBindingMeme1433 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1428, "Fire"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Water to Wind
    program30 = insert(Program.create(user1, library3, "Macro", "Published", "Water to Wind", "G", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1434 = insert(ProgramSequence.create(program30, 0, "Dew", 0.600000, "Am", 130.000000));
    sequence1435 = insert(ProgramSequence.create(program30, 0, "Fog", 0.600000, "C", 130.000000));
    sequence1436 = insert(ProgramSequence.create(program30, 0, "Rain", 0.600000, "G", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1437 = insert(ProgramSequenceBinding.create(sequence1436, 0));
    sequenceBinding1438 = insert(ProgramSequenceBinding.create(sequence1435, 1));
    sequenceBinding1439 = insert(ProgramSequenceBinding.create(sequence1434, 2));
    // Program Sequence Binding Memes
    sequenceBindingMeme1440 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1439, "Wind"));
    sequenceBindingMeme1441 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1438, "Water"));
    sequenceBindingMeme1442 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1438, "Wind"));
    sequenceBindingMeme1443 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1437, "Water"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Wind to Earth
    program18 = insert(Program.create(user1, library3, "Macro", "Published", "Wind to Earth", "Ebm", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1444 = insert(ProgramSequence.create(program18, 0, "Rolling Stone", 0.600000, "D", 130.000000));
    sequence1445 = insert(ProgramSequence.create(program18, 0, "Freedom", 0.600000, "Bm", 130.000000));
    sequence1446 = insert(ProgramSequence.create(program18, 0, "Open Road Tumbleweed", 0.600000, "Ebm", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1447 = insert(ProgramSequenceBinding.create(sequence1446, 0));
    sequenceBinding1448 = insert(ProgramSequenceBinding.create(sequence1445, 2));
    sequenceBinding1449 = insert(ProgramSequenceBinding.create(sequence1444, 1));
    // Program Sequence Binding Memes
    sequenceBindingMeme1450 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1448, "Earth"));
    sequenceBindingMeme1451 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1449, "Wind"));
    sequenceBindingMeme1452 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1447, "Wind"));
    sequenceBindingMeme1453 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1449, "Earth"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Wind to Fire
    program19 = insert(Program.create(user1, library3, "Macro", "Published", "Wind to Fire", "Bm", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1454 = insert(ProgramSequence.create(program19, 0, "Wilderness", 0.600000, "A", 130.000000));
    sequence1455 = insert(ProgramSequence.create(program19, 0, "Stoke the Flames", 0.600000, "Bm", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1456 = insert(ProgramSequenceBinding.create(sequence1455, 0));
    sequenceBinding1457 = insert(ProgramSequenceBinding.create(sequence1454, 1));
    // Program Sequence Binding Memes
    sequenceBindingMeme1458 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1457, "Fire"));
    sequenceBindingMeme1459 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1456, "Wind"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Macro-type Program Wind to Water
    program20 = insert(Program.create(user1, library3, "Macro", "Published", "Wind to Water", "Ebm", 130.000000, 0.000000));
    // Program Memes
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1460 = insert(ProgramSequence.create(program20, 0, "Afloat", 0.600000, "A", 130.000000));
    sequence1461 = insert(ProgramSequence.create(program20, 0, "Asink", 0.600000, "A", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding1462 = insert(ProgramSequenceBinding.create(sequence1461, 1));
    sequenceBinding1463 = insert(ProgramSequenceBinding.create(sequence1460, 0));
    // Program Sequence Binding Memes
    sequenceBindingMeme1464 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1463, "Wind"));
    sequenceBindingMeme1465 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1462, "Water"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Earth 3
    program81 = insert(Program.create(user27, library3, "Main", "Published", "Earth 3", "F", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program81, "Earth"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1466 = insert(ProgramSequence.create(program81, 16, "I", 0.600000, "F", 130.000000));
    sequence1467 = insert(ProgramSequence.create(program81, 32, "A", 0.600000, "F", 130.000000));
    sequence1468 = insert(ProgramSequence.create(program81, 16, "0", 0.600000, "F", 130.000000));
    // Program Sequence Chords
    sequenceChord1469 = insert(ProgramSequenceChord.create(sequence1468, 0.000000, "Gsus4/A"));
    sequenceChord1470 = insert(ProgramSequenceChord.create(sequence1467, 19.500000, "Bbmaj7add9"));
    sequenceChord1471 = insert(ProgramSequenceChord.create(sequence1466, 1.500000, "G-7"));
    sequenceChord1472 = insert(ProgramSequenceChord.create(sequence1466, 8.000000, "Fsus2/A"));
    sequenceChord1473 = insert(ProgramSequenceChord.create(sequence1467, 11.500000, "Fadd9/D"));
    sequenceChord1474 = insert(ProgramSequenceChord.create(sequence1467, 24.000000, "C/D"));
    sequenceChord1475 = insert(ProgramSequenceChord.create(sequence1467, 25.500000, "G-7/C"));
    sequenceChord1476 = insert(ProgramSequenceChord.create(sequence1467, 16.000000, "G-7"));
    sequenceChord1477 = insert(ProgramSequenceChord.create(sequence1467, 0.000000, "Fmaj7add9"));
    sequenceChord1478 = insert(ProgramSequenceChord.create(sequence1466, 9.500000, "Bbmaj7add9"));
    sequenceChord1479 = insert(ProgramSequenceChord.create(sequence1467, 25.500000, "Fadd4/G"));
    sequenceChord1480 = insert(ProgramSequenceChord.create(sequence1466, 0.000000, "D-7add9"));
    sequenceChord1481 = insert(ProgramSequenceChord.create(sequence1467, 3.500000, "Gsus4/A"));
    sequenceChord1482 = insert(ProgramSequenceChord.create(sequence1467, 7.500000, "Cmaj6"));
    // Program Sequence Bindings
    sequenceBinding1483 = insert(ProgramSequenceBinding.create(sequence1467, 7));
    sequenceBinding1484 = insert(ProgramSequenceBinding.create(sequence1466, 1));
    sequenceBinding1485 = insert(ProgramSequenceBinding.create(sequence1466, 0));
    sequenceBinding1486 = insert(ProgramSequenceBinding.create(sequence1466, 3));
    sequenceBinding1487 = insert(ProgramSequenceBinding.create(sequence1468, 9));
    sequenceBinding1488 = insert(ProgramSequenceBinding.create(sequence1467, 13));
    sequenceBinding1489 = insert(ProgramSequenceBinding.create(sequence1466, 17));
    sequenceBinding1490 = insert(ProgramSequenceBinding.create(sequence1468, 15));
    sequenceBinding1491 = insert(ProgramSequenceBinding.create(sequence1468, 16));
    sequenceBinding1492 = insert(ProgramSequenceBinding.create(sequence1468, 10));
    sequenceBinding1493 = insert(ProgramSequenceBinding.create(sequence1468, 11));
    sequenceBinding1494 = insert(ProgramSequenceBinding.create(sequence1467, 6));
    sequenceBinding1495 = insert(ProgramSequenceBinding.create(sequence1467, 5));
    sequenceBinding1496 = insert(ProgramSequenceBinding.create(sequence1467, 12));
    sequenceBinding1497 = insert(ProgramSequenceBinding.create(sequence1466, 19));
    sequenceBinding1498 = insert(ProgramSequenceBinding.create(sequence1468, 8));
    sequenceBinding1499 = insert(ProgramSequenceBinding.create(sequence1467, 4));
    sequenceBinding1500 = insert(ProgramSequenceBinding.create(sequence1466, 2));
    sequenceBinding1501 = insert(ProgramSequenceBinding.create(sequence1467, 14));
    sequenceBinding1502 = insert(ProgramSequenceBinding.create(sequence1466, 20));
    sequenceBinding1503 = insert(ProgramSequenceBinding.create(sequence1466, 18));
    // Program Sequence Binding Memes
    sequenceBindingMeme1504 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1494, "Steady"));
    sequenceBindingMeme1505 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1484, "Steady"));
    sequenceBindingMeme1506 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1491, "Break"));
    sequenceBindingMeme1507 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1496, "Steady"));
    sequenceBindingMeme1508 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1485, "Steady"));
    sequenceBindingMeme1509 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1486, "Break"));
    sequenceBindingMeme1510 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1498, "Steady"));
    sequenceBindingMeme1511 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1488, "Steady"));
    sequenceBindingMeme1512 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1493, "Break"));
    sequenceBindingMeme1513 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1502, "Break"));
    sequenceBindingMeme1514 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1495, "Steady"));
    sequenceBindingMeme1515 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1489, "Steady"));
    sequenceBindingMeme1516 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1500, "Steady"));
    sequenceBindingMeme1517 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1490, "Steady"));
    sequenceBindingMeme1518 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1487, "Steady"));
    sequenceBindingMeme1519 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1497, "Steady"));
    sequenceBindingMeme1520 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1501, "Steady"));
    sequenceBindingMeme1521 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1499, "Steady"));
    sequenceBindingMeme1522 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1503, "Steady"));
    sequenceBindingMeme1523 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1483, "Break"));
    sequenceBindingMeme1524 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1492, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Earth Earth
    program61 = insert(Program.create(user27, library3, "Main", "Published", "Earth Earth", "C", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program61, "Earth"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1525 = insert(ProgramSequence.create(program61, 32, "A", 0.600000, "C", 130.000000));
    // Program Sequence Chords
    sequenceChord1526 = insert(ProgramSequenceChord.create(sequence1525, 4.000000, "A-7"));
    sequenceChord1527 = insert(ProgramSequenceChord.create(sequence1525, 8.000000, "E-7"));
    sequenceChord1528 = insert(ProgramSequenceChord.create(sequence1525, 12.000000, "Fmaj6"));
    sequenceChord1529 = insert(ProgramSequenceChord.create(sequence1525, 16.000000, "C"));
    sequenceChord1530 = insert(ProgramSequenceChord.create(sequence1525, 24.000000, "D-7"));
    sequenceChord1531 = insert(ProgramSequenceChord.create(sequence1525, 20.000000, "A-7"));
    sequenceChord1532 = insert(ProgramSequenceChord.create(sequence1525, 29.500000, "F/G"));
    sequenceChord1533 = insert(ProgramSequenceChord.create(sequence1525, 27.500000, "Fmaj6"));
    sequenceChord1534 = insert(ProgramSequenceChord.create(sequence1525, 0.000000, "C"));
    // Program Sequence Bindings
    sequenceBinding1535 = insert(ProgramSequenceBinding.create(sequence1525, 12));
    sequenceBinding1536 = insert(ProgramSequenceBinding.create(sequence1525, 15));
    sequenceBinding1537 = insert(ProgramSequenceBinding.create(sequence1525, 3));
    sequenceBinding1538 = insert(ProgramSequenceBinding.create(sequence1525, 0));
    sequenceBinding1539 = insert(ProgramSequenceBinding.create(sequence1525, 7));
    sequenceBinding1540 = insert(ProgramSequenceBinding.create(sequence1525, 9));
    sequenceBinding1541 = insert(ProgramSequenceBinding.create(sequence1525, 13));
    sequenceBinding1542 = insert(ProgramSequenceBinding.create(sequence1525, 14));
    sequenceBinding1543 = insert(ProgramSequenceBinding.create(sequence1525, 4));
    sequenceBinding1544 = insert(ProgramSequenceBinding.create(sequence1525, 8));
    sequenceBinding1545 = insert(ProgramSequenceBinding.create(sequence1525, 16));
    sequenceBinding1546 = insert(ProgramSequenceBinding.create(sequence1525, 2));
    sequenceBinding1547 = insert(ProgramSequenceBinding.create(sequence1525, 11));
    sequenceBinding1548 = insert(ProgramSequenceBinding.create(sequence1525, 5));
    sequenceBinding1549 = insert(ProgramSequenceBinding.create(sequence1525, 10));
    sequenceBinding1550 = insert(ProgramSequenceBinding.create(sequence1525, 6));
    sequenceBinding1551 = insert(ProgramSequenceBinding.create(sequence1525, 1));
    // Program Sequence Binding Memes
    sequenceBindingMeme1552 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1538, "Steady"));
    sequenceBindingMeme1553 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1539, "Wind"));
    sequenceBindingMeme1554 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1536, "Wind"));
    sequenceBindingMeme1555 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1549, "Steady"));
    sequenceBindingMeme1556 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1537, "Break"));
    sequenceBindingMeme1557 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1542, "Break"));
    sequenceBindingMeme1558 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1547, "Break"));
    sequenceBindingMeme1559 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1544, "Steady"));
    sequenceBindingMeme1560 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1535, "Steady"));
    sequenceBindingMeme1561 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1541, "Fire"));
    sequenceBindingMeme1562 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1537, "Water"));
    sequenceBindingMeme1563 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1548, "Steady"));
    sequenceBindingMeme1564 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1540, "Steady"));
    sequenceBindingMeme1565 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1535, "Fire"));
    sequenceBindingMeme1566 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1542, "Water"));
    sequenceBindingMeme1567 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1551, "Steady"));
    sequenceBindingMeme1568 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1550, "Steady"));
    sequenceBindingMeme1569 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1541, "Steady"));
    sequenceBindingMeme1570 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1546, "Steady"));
    sequenceBindingMeme1571 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1539, "Break"));
    sequenceBindingMeme1572 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1545, "Break"));
    sequenceBindingMeme1573 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1536, "Break"));
    sequenceBindingMeme1574 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1545, "Fire"));
    sequenceBindingMeme1575 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1543, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Earth First
    program47 = insert(Program.create(user27, library3, "Main", "Published", "Earth First", "Bb", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program47, "Earth"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1576 = insert(ProgramSequence.create(program47, 32, "Chorus", 0.600000, "Bb", 130.000000));
    sequence1577 = insert(ProgramSequence.create(program47, 8, "Interlude Big", 0.600000, "Bb", 130.000000));
    sequence1578 = insert(ProgramSequence.create(program47, 8, "Interlude Small", 0.600000, "Bb", 130.000000));
    sequence1579 = insert(ProgramSequence.create(program47, 16, "Verse", 0.600000, "Bb", 130.000000));
    // Program Sequence Chords
    sequenceChord1580 = insert(ProgramSequenceChord.create(sequence1576, 8.000000, "Eb"));
    sequenceChord1581 = insert(ProgramSequenceChord.create(sequence1576, 4.000000, "G-"));
    sequenceChord1582 = insert(ProgramSequenceChord.create(sequence1579, 24.000000, "Eb"));
    sequenceChord1583 = insert(ProgramSequenceChord.create(sequence1578, 1.500000, "Fsus4"));
    sequenceChord1584 = insert(ProgramSequenceChord.create(sequence1576, 16.000000, "C-"));
    sequenceChord1585 = insert(ProgramSequenceChord.create(sequence1579, 20.000000, "G-"));
    sequenceChord1586 = insert(ProgramSequenceChord.create(sequence1576, 0.000000, "F"));
    sequenceChord1587 = insert(ProgramSequenceChord.create(sequence1577, 1.500000, "Fsus4"));
    sequenceChord1588 = insert(ProgramSequenceChord.create(sequence1579, 16.000000, "Bb"));
    sequenceChord1589 = insert(ProgramSequenceChord.create(sequence1576, 20.000000, "G-"));
    sequenceChord1590 = insert(ProgramSequenceChord.create(sequence1579, 4.000000, "D-"));
    sequenceChord1591 = insert(ProgramSequenceChord.create(sequence1576, 24.000000, "Eb"));
    sequenceChord1592 = insert(ProgramSequenceChord.create(sequence1578, 0.000000, "D-"));
    sequenceChord1593 = insert(ProgramSequenceChord.create(sequence1577, 0.000000, "D-"));
    sequenceChord1594 = insert(ProgramSequenceChord.create(sequence1579, 8.000000, "Eb"));
    sequenceChord1595 = insert(ProgramSequenceChord.create(sequence1576, 12.000000, "Bb"));
    sequenceChord1596 = insert(ProgramSequenceChord.create(sequence1579, 0.000000, "Bb"));
    // Program Sequence Bindings
    sequenceBinding1597 = insert(ProgramSequenceBinding.create(sequence1579, 2));
    sequenceBinding1598 = insert(ProgramSequenceBinding.create(sequence1576, 17));
    sequenceBinding1599 = insert(ProgramSequenceBinding.create(sequence1579, 14));
    sequenceBinding1600 = insert(ProgramSequenceBinding.create(sequence1579, 5));
    sequenceBinding1601 = insert(ProgramSequenceBinding.create(sequence1577, 21));
    sequenceBinding1602 = insert(ProgramSequenceBinding.create(sequence1578, 0));
    sequenceBinding1603 = insert(ProgramSequenceBinding.create(sequence1579, 12));
    sequenceBinding1604 = insert(ProgramSequenceBinding.create(sequence1578, 22));
    sequenceBinding1605 = insert(ProgramSequenceBinding.create(sequence1579, 13));
    sequenceBinding1606 = insert(ProgramSequenceBinding.create(sequence1576, 7));
    sequenceBinding1607 = insert(ProgramSequenceBinding.create(sequence1577, 19));
    sequenceBinding1608 = insert(ProgramSequenceBinding.create(sequence1576, 16));
    sequenceBinding1609 = insert(ProgramSequenceBinding.create(sequence1577, 9));
    sequenceBinding1610 = insert(ProgramSequenceBinding.create(sequence1576, 6));
    sequenceBinding1611 = insert(ProgramSequenceBinding.create(sequence1578, 10));
    sequenceBinding1612 = insert(ProgramSequenceBinding.create(sequence1579, 3));
    sequenceBinding1613 = insert(ProgramSequenceBinding.create(sequence1577, 8));
    sequenceBinding1614 = insert(ProgramSequenceBinding.create(sequence1578, 1));
    sequenceBinding1615 = insert(ProgramSequenceBinding.create(sequence1577, 18));
    sequenceBinding1616 = insert(ProgramSequenceBinding.create(sequence1577, 20));
    sequenceBinding1617 = insert(ProgramSequenceBinding.create(sequence1578, 25));
    sequenceBinding1618 = insert(ProgramSequenceBinding.create(sequence1579, 4));
    sequenceBinding1619 = insert(ProgramSequenceBinding.create(sequence1578, 24));
    sequenceBinding1620 = insert(ProgramSequenceBinding.create(sequence1578, 11));
    sequenceBinding1621 = insert(ProgramSequenceBinding.create(sequence1579, 15));
    sequenceBinding1622 = insert(ProgramSequenceBinding.create(sequence1578, 23));
    // Program Sequence Binding Memes
    sequenceBindingMeme1623 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1618, "Steady"));
    sequenceBindingMeme1624 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1607, "Large"));
    sequenceBindingMeme1625 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1609, "Large"));
    sequenceBindingMeme1626 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1608, "Steady"));
    sequenceBindingMeme1627 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1602, "Small"));
    sequenceBindingMeme1628 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1601, "Large"));
    sequenceBindingMeme1629 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1615, "Break"));
    sequenceBindingMeme1630 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1614, "Break"));
    sequenceBindingMeme1631 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1610, "Steady"));
    sequenceBindingMeme1632 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1617, "Small"));
    sequenceBindingMeme1633 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1622, "Break"));
    sequenceBindingMeme1634 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1604, "Break"));
    sequenceBindingMeme1635 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1606, "Steady"));
    sequenceBindingMeme1636 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1603, "Steady"));
    sequenceBindingMeme1637 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1607, "Break"));
    sequenceBindingMeme1638 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1602, "Break"));
    sequenceBindingMeme1639 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1601, "Break"));
    sequenceBindingMeme1640 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1609, "Break"));
    sequenceBindingMeme1641 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1619, "Break"));
    sequenceBindingMeme1642 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1615, "Large"));
    sequenceBindingMeme1643 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1605, "Steady"));
    sequenceBindingMeme1644 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1619, "Small"));
    sequenceBindingMeme1645 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1611, "Small"));
    sequenceBindingMeme1646 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1612, "Steady"));
    sequenceBindingMeme1647 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1621, "Steady"));
    sequenceBindingMeme1648 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1616, "Large"));
    sequenceBindingMeme1649 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1599, "Steady"));
    sequenceBindingMeme1650 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1614, "Small"));
    sequenceBindingMeme1651 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1620, "Break"));
    sequenceBindingMeme1652 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1616, "Break"));
    sequenceBindingMeme1653 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1604, "Small"));
    sequenceBindingMeme1654 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1611, "Break"));
    sequenceBindingMeme1655 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1597, "Steady"));
    sequenceBindingMeme1656 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1600, "Steady"));
    sequenceBindingMeme1657 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1613, "Break"));
    sequenceBindingMeme1658 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1598, "Steady"));
    sequenceBindingMeme1659 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1622, "Small"));
    sequenceBindingMeme1660 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1617, "Break"));
    sequenceBindingMeme1661 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1620, "Small"));
    sequenceBindingMeme1662 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1613, "Large"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


  }

  private void go11() throws Exception {
    // Insert Main-type Program Earth Knyght
    program66 = insert(Program.create(user27, library3, "Main", "Published", "Earth Knyght", "Bb-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program66, "Earth"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1663 = insert(ProgramSequence.create(program66, 32, "I", 0.600000, "Bb-", 130.000000));
    sequence1664 = insert(ProgramSequence.create(program66, 48, "II", 0.600000, "Bb-", 130.000000));
    sequence1665 = insert(ProgramSequence.create(program66, 64, "A", 0.600000, "Bb-", 130.000000));
    // Program Sequence Chords
    sequenceChord1666 = insert(ProgramSequenceChord.create(sequence1665, 32.000000, "Bb-"));
    sequenceChord1667 = insert(ProgramSequenceChord.create(sequence1665, 8.000000, "Abmaj6/9"));
    sequenceChord1668 = insert(ProgramSequenceChord.create(sequence1665, 60.000000, "F"));
    sequenceChord1669 = insert(ProgramSequenceChord.create(sequence1665, 16.000000, "Gbmaj7"));
    sequenceChord1670 = insert(ProgramSequenceChord.create(sequence1665, 24.000000, "Db"));
    sequenceChord1671 = insert(ProgramSequenceChord.create(sequence1664, 0.000000, "Bb-"));
    sequenceChord1672 = insert(ProgramSequenceChord.create(sequence1665, 40.000000, "Abmaj6/9"));
    sequenceChord1673 = insert(ProgramSequenceChord.create(sequence1665, 0.000000, "Bb-"));
    sequenceChord1674 = insert(ProgramSequenceChord.create(sequence1665, 48.000000, "Gbmaj7"));
    sequenceChord1675 = insert(ProgramSequenceChord.create(sequence1663, 0.000000, "Bb-"));
    sequenceChord1676 = insert(ProgramSequenceChord.create(sequence1665, 28.000000, "Eb-"));
    sequenceChord1677 = insert(ProgramSequenceChord.create(sequence1665, 56.000000, "Fsus4"));
    // Program Sequence Bindings
    sequenceBinding1678 = insert(ProgramSequenceBinding.create(sequence1663, 6));
    sequenceBinding1679 = insert(ProgramSequenceBinding.create(sequence1665, 7));
    sequenceBinding1680 = insert(ProgramSequenceBinding.create(sequence1665, 10));
    sequenceBinding1681 = insert(ProgramSequenceBinding.create(sequence1663, 5));
    sequenceBinding1682 = insert(ProgramSequenceBinding.create(sequence1664, 5));
    sequenceBinding1683 = insert(ProgramSequenceBinding.create(sequence1665, 8));
    sequenceBinding1684 = insert(ProgramSequenceBinding.create(sequence1665, 3));
    sequenceBinding1685 = insert(ProgramSequenceBinding.create(sequence1665, 2));
    sequenceBinding1686 = insert(ProgramSequenceBinding.create(sequence1665, 1));
    sequenceBinding1687 = insert(ProgramSequenceBinding.create(sequence1665, 9));
    sequenceBinding1688 = insert(ProgramSequenceBinding.create(sequence1665, 4));
    sequenceBinding1689 = insert(ProgramSequenceBinding.create(sequence1663, 0));
    sequenceBinding1690 = insert(ProgramSequenceBinding.create(sequence1664, 6));
    // Program Sequence Binding Memes
    sequenceBindingMeme1691 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1690, "Break"));
    sequenceBindingMeme1692 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1688, "Steady"));
    sequenceBindingMeme1693 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1684, "Steady"));
    sequenceBindingMeme1694 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1686, "Steady"));
    sequenceBindingMeme1695 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1678, "Steady"));
    sequenceBindingMeme1696 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1683, "Steady"));
    sequenceBindingMeme1697 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1679, "Steady"));
    sequenceBindingMeme1698 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1680, "Break"));
    sequenceBindingMeme1699 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1685, "Steady"));
    sequenceBindingMeme1700 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1682, "Break"));
    sequenceBindingMeme1701 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1689, "Steady"));
    sequenceBindingMeme1702 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1687, "Steady"));
    sequenceBindingMeme1703 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1681, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Earth Mert
    program79 = insert(Program.create(user27, library3, "Main", "Published", "Earth Mert", "Db", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program79, "Earth"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1704 = insert(ProgramSequence.create(program79, 16, "0", 0.600000, "Db", 130.000000));
    sequence1705 = insert(ProgramSequence.create(program79, 32, "A", 0.600000, "Db", 130.000000));
    // Program Sequence Chords
    sequenceChord1706 = insert(ProgramSequenceChord.create(sequence1705, 3.500000, "Dbsus2/F"));
    sequenceChord1707 = insert(ProgramSequenceChord.create(sequence1705, 19.500000, "Dbsus2/F"));
    sequenceChord1708 = insert(ProgramSequenceChord.create(sequence1705, 27.500000, "Dbadd9"));
    sequenceChord1709 = insert(ProgramSequenceChord.create(sequence1704, 4.000000, "Gb/Ab"));
    sequenceChord1710 = insert(ProgramSequenceChord.create(sequence1705, 7.500000, "Eb-7add11"));
    sequenceChord1711 = insert(ProgramSequenceChord.create(sequence1705, 23.500000, "Gbmaj7/Ab"));
    sequenceChord1712 = insert(ProgramSequenceChord.create(sequence1705, 0.000000, "Gbmaj7"));
    sequenceChord1713 = insert(ProgramSequenceChord.create(sequence1705, 29.500000, "G7#11"));
    sequenceChord1714 = insert(ProgramSequenceChord.create(sequence1705, 16.000000, "Gbmaj7"));
    sequenceChord1715 = insert(ProgramSequenceChord.create(sequence1704, 1.500000, "Gbadd9/Db"));
    sequenceChord1716 = insert(ProgramSequenceChord.create(sequence1705, 11.500000, "Bb-7add11"));
    sequenceChord1717 = insert(ProgramSequenceChord.create(sequence1704, 0.000000, "Gbadd9"));
    // Program Sequence Bindings
    sequenceBinding1718 = insert(ProgramSequenceBinding.create(sequence1705, 14));
    sequenceBinding1719 = insert(ProgramSequenceBinding.create(sequence1704, 8));
    sequenceBinding1720 = insert(ProgramSequenceBinding.create(sequence1704, 3));
    sequenceBinding1721 = insert(ProgramSequenceBinding.create(sequence1705, 5));
    sequenceBinding1722 = insert(ProgramSequenceBinding.create(sequence1704, 9));
    sequenceBinding1723 = insert(ProgramSequenceBinding.create(sequence1705, 6));
    sequenceBinding1724 = insert(ProgramSequenceBinding.create(sequence1704, 2));
    sequenceBinding1725 = insert(ProgramSequenceBinding.create(sequence1705, 7));
    sequenceBinding1726 = insert(ProgramSequenceBinding.create(sequence1704, 10));
    sequenceBinding1727 = insert(ProgramSequenceBinding.create(sequence1705, 4));
    sequenceBinding1728 = insert(ProgramSequenceBinding.create(sequence1704, 1));
    sequenceBinding1729 = insert(ProgramSequenceBinding.create(sequence1704, 0));
    sequenceBinding1730 = insert(ProgramSequenceBinding.create(sequence1705, 12));
    sequenceBinding1731 = insert(ProgramSequenceBinding.create(sequence1704, 11));
    sequenceBinding1732 = insert(ProgramSequenceBinding.create(sequence1705, 13));
    // Program Sequence Binding Memes
    sequenceBindingMeme1733 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1719, "Steady"));
    sequenceBindingMeme1734 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1729, "Steady"));
    sequenceBindingMeme1735 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1718, "Break"));
    sequenceBindingMeme1736 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1726, "Steady"));
    sequenceBindingMeme1737 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1724, "Steady"));
    sequenceBindingMeme1738 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1732, "Steady"));
    sequenceBindingMeme1739 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1731, "Break"));
    sequenceBindingMeme1740 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1728, "Steady"));
    sequenceBindingMeme1741 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1725, "Break"));
    sequenceBindingMeme1742 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1727, "Steady"));
    sequenceBindingMeme1743 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1722, "Steady"));
    sequenceBindingMeme1744 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1721, "Steady"));
    sequenceBindingMeme1745 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1730, "Steady"));
    sequenceBindingMeme1746 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1720, "Break"));
    sequenceBindingMeme1747 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1723, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Earth Rudy
    program52 = insert(Program.create(user27, library3, "Main", "Published", "Earth Rudy", "D", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program52, "Earth"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1748 = insert(ProgramSequence.create(program52, 48, "C.", 0.600000, "D", 130.000000));
    sequence1749 = insert(ProgramSequence.create(program52, 32, "B", 0.600000, "D", 130.000000));
    sequence1750 = insert(ProgramSequence.create(program52, 32, "C", 0.600000, "D", 130.000000));
    sequence1751 = insert(ProgramSequence.create(program52, 32, "A", 0.600000, "D", 130.000000));
    // Program Sequence Chords
    sequenceChord1752 = insert(ProgramSequenceChord.create(sequence1751, 16.000000, "D"));
    sequenceChord1753 = insert(ProgramSequenceChord.create(sequence1749, 16.000000, "B-"));
    sequenceChord1754 = insert(ProgramSequenceChord.create(sequence1751, 3.500000, "F#-"));
    sequenceChord1755 = insert(ProgramSequenceChord.create(sequence1751, 29.500000, "Gmaj7/A"));
    sequenceChord1756 = insert(ProgramSequenceChord.create(sequence1750, 0.000000, "F#-"));
    sequenceChord1757 = insert(ProgramSequenceChord.create(sequence1751, 7.500000, "G"));
    sequenceChord1758 = insert(ProgramSequenceChord.create(sequence1749, 28.000000, "G"));
    sequenceChord1759 = insert(ProgramSequenceChord.create(sequence1751, 13.500000, "E-7"));
    sequenceChord1760 = insert(ProgramSequenceChord.create(sequence1749, 12.000000, "G"));
    sequenceChord1761 = insert(ProgramSequenceChord.create(sequence1749, 4.000000, "E-"));
    sequenceChord1762 = insert(ProgramSequenceChord.create(sequence1749, 20.000000, "E-"));
    sequenceChord1763 = insert(ProgramSequenceChord.create(sequence1751, 19.500000, "F#-"));
    sequenceChord1764 = insert(ProgramSequenceChord.create(sequence1751, 23.500000, "G"));
    sequenceChord1765 = insert(ProgramSequenceChord.create(sequence1749, 0.000000, "B-"));
    sequenceChord1766 = insert(ProgramSequenceChord.create(sequence1751, 0.000000, "D"));
    // Program Sequence Bindings
    sequenceBinding1767 = insert(ProgramSequenceBinding.create(sequence1748, 11));
    sequenceBinding1768 = insert(ProgramSequenceBinding.create(sequence1750, 11));
    sequenceBinding1769 = insert(ProgramSequenceBinding.create(sequence1749, 9));
    sequenceBinding1770 = insert(ProgramSequenceBinding.create(sequence1751, 2));
    sequenceBinding1771 = insert(ProgramSequenceBinding.create(sequence1751, 7));
    sequenceBinding1772 = insert(ProgramSequenceBinding.create(sequence1749, 8));
    sequenceBinding1773 = insert(ProgramSequenceBinding.create(sequence1751, 1));
    sequenceBinding1774 = insert(ProgramSequenceBinding.create(sequence1749, 10));
    sequenceBinding1775 = insert(ProgramSequenceBinding.create(sequence1748, 12));
    sequenceBinding1776 = insert(ProgramSequenceBinding.create(sequence1751, 5));
    sequenceBinding1777 = insert(ProgramSequenceBinding.create(sequence1751, 0));
    sequenceBinding1778 = insert(ProgramSequenceBinding.create(sequence1749, 3));
    sequenceBinding1779 = insert(ProgramSequenceBinding.create(sequence1751, 6));
    sequenceBinding1780 = insert(ProgramSequenceBinding.create(sequence1750, 12));
    sequenceBinding1781 = insert(ProgramSequenceBinding.create(sequence1749, 4));
    // Program Sequence Binding Memes
    sequenceBindingMeme1782 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1780, "Steady"));
    sequenceBindingMeme1783 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1770, "Steady"));
    sequenceBindingMeme1784 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1773, "Steady"));
    sequenceBindingMeme1785 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1776, "Steady"));
    sequenceBindingMeme1786 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1775, "Break"));
    sequenceBindingMeme1787 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1778, "Steady"));
    sequenceBindingMeme1788 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1779, "Steady"));
    sequenceBindingMeme1789 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1772, "Steady"));
    sequenceBindingMeme1790 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1774, "Steady"));
    sequenceBindingMeme1791 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1769, "Steady"));
    sequenceBindingMeme1792 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1771, "Break"));
    sequenceBindingMeme1793 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1781, "Steady"));
    sequenceBindingMeme1794 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1768, "Steady"));
    sequenceBindingMeme1795 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1777, "Steady"));
    sequenceBindingMeme1796 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1767, "Break"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Earth'm
    program88 = insert(Program.create(user27, library3, "Main", "Published", "Earth'm", "F#", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program88, "Earth"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1797 = insert(ProgramSequence.create(program88, 32, "A", 0.600000, "F#", 130.000000));
    sequence1798 = insert(ProgramSequence.create(program88, 32, "B", 0.600000, "F#", 130.000000));
    // Program Sequence Chords
    sequenceChord1799 = insert(ProgramSequenceChord.create(sequence1797, 3.500000, "Bsus2/D#"));
    sequenceChord1800 = insert(ProgramSequenceChord.create(sequence1797, 7.500000, "F#/C#"));
    sequenceChord1801 = insert(ProgramSequenceChord.create(sequence1797, 23.500000, "F#/C#"));
    sequenceChord1802 = insert(ProgramSequenceChord.create(sequence1797, 31.500000, "F#"));
    sequenceChord1803 = insert(ProgramSequenceChord.create(sequence1798, 31.500000, "F#"));
    sequenceChord1804 = insert(ProgramSequenceChord.create(sequence1797, 0.000000, "F#add9"));
    sequenceChord1805 = insert(ProgramSequenceChord.create(sequence1797, 0.000000, "F#"));
    sequenceChord1806 = insert(ProgramSequenceChord.create(sequence1797, 15.500000, "F#add9"));
    sequenceChord1807 = insert(ProgramSequenceChord.create(sequence1797, 11.500000, "G#-7"));
    sequenceChord1808 = insert(ProgramSequenceChord.create(sequence1797, 29.500000, "C#add4/B"));
    sequenceChord1809 = insert(ProgramSequenceChord.create(sequence1798, 23.500000, "F#/A#"));
    sequenceChord1810 = insert(ProgramSequenceChord.create(sequence1798, 0.000000, "D#-7"));
    sequenceChord1811 = insert(ProgramSequenceChord.create(sequence1798, 29.500000, "C#add4"));
    sequenceChord1812 = insert(ProgramSequenceChord.create(sequence1797, 19.500000, "Bsus2/D#"));
    sequenceChord1813 = insert(ProgramSequenceChord.create(sequence1798, 31.500000, "D#-7"));
    sequenceChord1814 = insert(ProgramSequenceChord.create(sequence1797, 31.500000, "F#add9"));
    sequenceChord1815 = insert(ProgramSequenceChord.create(sequence1798, 3.500000, "Bsus2/D#"));
    sequenceChord1816 = insert(ProgramSequenceChord.create(sequence1797, 27.500000, "F#sus2/A#"));
    sequenceChord1817 = insert(ProgramSequenceChord.create(sequence1797, 29.500000, "Bmaj7add9"));
    sequenceChord1818 = insert(ProgramSequenceChord.create(sequence1798, 7.500000, "D#-7"));
    sequenceChord1819 = insert(ProgramSequenceChord.create(sequence1797, 15.500000, "F#"));
    sequenceChord1820 = insert(ProgramSequenceChord.create(sequence1797, 13.500000, "D#-7"));
    sequenceChord1821 = insert(ProgramSequenceChord.create(sequence1798, 19.500000, "Bsus2/G#"));
    // Program Sequence Bindings
    sequenceBinding1822 = insert(ProgramSequenceBinding.create(sequence1797, 10));
    sequenceBinding1823 = insert(ProgramSequenceBinding.create(sequence1797, 13));
    sequenceBinding1824 = insert(ProgramSequenceBinding.create(sequence1797, 4));
    sequenceBinding1825 = insert(ProgramSequenceBinding.create(sequence1798, 9));
    sequenceBinding1826 = insert(ProgramSequenceBinding.create(sequence1798, 6));
    sequenceBinding1827 = insert(ProgramSequenceBinding.create(sequence1798, 8));
    sequenceBinding1828 = insert(ProgramSequenceBinding.create(sequence1797, 11));
    sequenceBinding1829 = insert(ProgramSequenceBinding.create(sequence1797, 2));
    sequenceBinding1830 = insert(ProgramSequenceBinding.create(sequence1797, 14));
    sequenceBinding1831 = insert(ProgramSequenceBinding.create(sequence1798, 7));
    sequenceBinding1832 = insert(ProgramSequenceBinding.create(sequence1797, 15));
    sequenceBinding1833 = insert(ProgramSequenceBinding.create(sequence1797, 5));
    sequenceBinding1834 = insert(ProgramSequenceBinding.create(sequence1798, 0));
    sequenceBinding1835 = insert(ProgramSequenceBinding.create(sequence1797, 3));
    sequenceBinding1836 = insert(ProgramSequenceBinding.create(sequence1798, 1));
    sequenceBinding1837 = insert(ProgramSequenceBinding.create(sequence1797, 12));
    // Program Sequence Binding Memes
    sequenceBindingMeme1838 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1829, "Steady"));
    sequenceBindingMeme1839 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1832, "Break"));
    sequenceBindingMeme1840 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1830, "Steady"));
    sequenceBindingMeme1841 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1833, "Break"));
    sequenceBindingMeme1842 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1834, "Steady"));
    sequenceBindingMeme1843 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1828, "Steady"));
    sequenceBindingMeme1844 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1824, "Steady"));
    sequenceBindingMeme1845 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1837, "Steady"));
    sequenceBindingMeme1846 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1835, "Steady"));
    sequenceBindingMeme1847 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1825, "Break"));
    sequenceBindingMeme1848 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1831, "Steady"));
    sequenceBindingMeme1849 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1827, "Steady"));
    sequenceBindingMeme1850 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1836, "Steady"));
    sequenceBindingMeme1851 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1823, "Steady"));
    sequenceBindingMeme1852 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1826, "Steady"));
    sequenceBindingMeme1853 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1822, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Earthen Satay
    program59 = insert(Program.create(user27, library3, "Main", "Published", "Earthen Satay", "Db", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program59, "Earth"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1854 = insert(ProgramSequence.create(program59, 32, "B", 0.600000, "Db", 130.000000));
    sequence1855 = insert(ProgramSequence.create(program59, 16, "A", 0.600000, "Db", 130.000000));
    sequence1856 = insert(ProgramSequence.create(program59, 16, "C", 0.600000, "Db", 130.000000));
    // Program Sequence Chords
    sequenceChord1857 = insert(ProgramSequenceChord.create(sequence1856, 0.000000, "F-7"));
    sequenceChord1858 = insert(ProgramSequenceChord.create(sequence1854, 14.000000, "Db/F"));
    sequenceChord1859 = insert(ProgramSequenceChord.create(sequence1856, 12.000000, "Ab7sus4"));
    sequenceChord1860 = insert(ProgramSequenceChord.create(sequence1855, 8.000000, "Db"));
    sequenceChord1861 = insert(ProgramSequenceChord.create(sequence1856, 8.000000, "Gbmaj6/9"));
    sequenceChord1862 = insert(ProgramSequenceChord.create(sequence1854, 0.000000, "Gb"));
    sequenceChord1863 = insert(ProgramSequenceChord.create(sequence1854, 24.000000, "Eb-"));
    sequenceChord1864 = insert(ProgramSequenceChord.create(sequence1854, 6.000000, "Bb-"));
    sequenceChord1865 = insert(ProgramSequenceChord.create(sequence1854, 20.000000, "Ab"));
    sequenceChord1866 = insert(ProgramSequenceChord.create(sequence1854, 16.000000, "Gb"));
    sequenceChord1867 = insert(ProgramSequenceChord.create(sequence1854, 22.000000, "Bb-"));
    sequenceChord1868 = insert(ProgramSequenceChord.create(sequence1854, 28.000000, "Ab7sus4"));
    sequenceChord1869 = insert(ProgramSequenceChord.create(sequence1855, 0.000000, "Bb-"));
    sequenceChord1870 = insert(ProgramSequenceChord.create(sequence1854, 8.000000, "Eb-"));
    sequenceChord1871 = insert(ProgramSequenceChord.create(sequence1854, 4.000000, "Ab"));
    // Program Sequence Bindings
    sequenceBinding1872 = insert(ProgramSequenceBinding.create(sequence1855, 0));
    sequenceBinding1873 = insert(ProgramSequenceBinding.create(sequence1855, 2));
    sequenceBinding1874 = insert(ProgramSequenceBinding.create(sequence1855, 9));
    sequenceBinding1875 = insert(ProgramSequenceBinding.create(sequence1855, 10));
    sequenceBinding1876 = insert(ProgramSequenceBinding.create(sequence1856, 20));
    sequenceBinding1877 = insert(ProgramSequenceBinding.create(sequence1855, 1));
    sequenceBinding1878 = insert(ProgramSequenceBinding.create(sequence1855, 11));
    sequenceBinding1879 = insert(ProgramSequenceBinding.create(sequence1856, 21));
    sequenceBinding1880 = insert(ProgramSequenceBinding.create(sequence1855, 3));
    sequenceBinding1881 = insert(ProgramSequenceBinding.create(sequence1856, 16));
    sequenceBinding1882 = insert(ProgramSequenceBinding.create(sequence1856, 18));
    sequenceBinding1883 = insert(ProgramSequenceBinding.create(sequence1855, 7));
    sequenceBinding1884 = insert(ProgramSequenceBinding.create(sequence1855, 8));
    sequenceBinding1885 = insert(ProgramSequenceBinding.create(sequence1854, 15));
    sequenceBinding1886 = insert(ProgramSequenceBinding.create(sequence1856, 17));
    sequenceBinding1887 = insert(ProgramSequenceBinding.create(sequence1856, 19));
    sequenceBinding1888 = insert(ProgramSequenceBinding.create(sequence1854, 14));
    sequenceBinding1889 = insert(ProgramSequenceBinding.create(sequence1854, 5));
    sequenceBinding1890 = insert(ProgramSequenceBinding.create(sequence1855, 6));
    sequenceBinding1891 = insert(ProgramSequenceBinding.create(sequence1854, 12));
    sequenceBinding1892 = insert(ProgramSequenceBinding.create(sequence1854, 13));
    sequenceBinding1893 = insert(ProgramSequenceBinding.create(sequence1854, 4));
    // Program Sequence Binding Memes
    sequenceBindingMeme1894 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1893, "Steady"));
    sequenceBindingMeme1895 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1886, "Steady"));
    sequenceBindingMeme1896 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1877, "Steady"));
    sequenceBindingMeme1897 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1875, "Steady"));
    sequenceBindingMeme1898 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1879, "Break"));
    sequenceBindingMeme1899 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1876, "Steady"));
    sequenceBindingMeme1900 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1888, "Steady"));
    sequenceBindingMeme1901 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1882, "Break"));
    sequenceBindingMeme1902 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1884, "Steady"));
    sequenceBindingMeme1903 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1878, "Break"));
    sequenceBindingMeme1904 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1891, "Steady"));
    sequenceBindingMeme1905 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1892, "Steady"));
    sequenceBindingMeme1906 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1885, "Steady"));
    sequenceBindingMeme1907 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1889, "Steady"));
    sequenceBindingMeme1908 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1887, "Steady"));
    sequenceBindingMeme1909 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1881, "Steady"));
    sequenceBindingMeme1910 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1874, "Steady"));
    sequenceBindingMeme1911 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1890, "Steady"));
    sequenceBindingMeme1912 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1883, "Steady"));
    sequenceBindingMeme1913 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1872, "Steady"));
    sequenceBindingMeme1914 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1873, "Steady"));
    sequenceBindingMeme1915 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1880, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Fire Babes
    program48 = insert(Program.create(user27, library3, "Main", "Published", "Fire Babes", "E-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program48, "Fire"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence1916 = insert(ProgramSequence.create(program48, 16, "Verse", 0.600000, "E-", 130.000000));
    sequence1917 = insert(ProgramSequence.create(program48, 16, "I", 0.600000, "E-", 130.000000));
    sequence1918 = insert(ProgramSequence.create(program48, 32, "Bridge", 0.600000, "E-", 130.000000));
    sequence1919 = insert(ProgramSequence.create(program48, 24, "II", 0.600000, "E-", 130.000000));
    sequence1920 = insert(ProgramSequence.create(program48, 32, "Prechorus", 0.600000, "E-", 130.000000));
    sequence1921 = insert(ProgramSequence.create(program48, 16, "Chorus", 0.600000, "E-", 130.000000));
    // Program Sequence Chords
    sequenceChord1922 = insert(ProgramSequenceChord.create(sequence1920, 24.000000, "C/D"));
    sequenceChord1923 = insert(ProgramSequenceChord.create(sequence1920, 16.000000, "F"));
    sequenceChord1924 = insert(ProgramSequenceChord.create(sequence1917, 0.000000, "E-"));
    sequenceChord1925 = insert(ProgramSequenceChord.create(sequence1916, 8.000000, "G"));
    sequenceChord1926 = insert(ProgramSequenceChord.create(sequence1918, 16.000000, "D-"));
    sequenceChord1927 = insert(ProgramSequenceChord.create(sequence1916, 12.000000, "A"));
    sequenceChord1928 = insert(ProgramSequenceChord.create(sequence1920, 4.000000, "Bsus4"));
    sequenceChord1929 = insert(ProgramSequenceChord.create(sequence1918, 0.000000, "F"));
    sequenceChord1930 = insert(ProgramSequenceChord.create(sequence1920, 8.000000, "C"));
    sequenceChord1931 = insert(ProgramSequenceChord.create(sequence1920, 12.000000, "G"));
    sequenceChord1932 = insert(ProgramSequenceChord.create(sequence1921, 8.000000, "A-"));
    sequenceChord1933 = insert(ProgramSequenceChord.create(sequence1921, 0.000000, "E-"));
    sequenceChord1934 = insert(ProgramSequenceChord.create(sequence1918, 8.000000, "A-"));
    sequenceChord1935 = insert(ProgramSequenceChord.create(sequence1916, 0.000000, "E-"));
    sequenceChord1936 = insert(ProgramSequenceChord.create(sequence1921, 12.000000, "G"));
    sequenceChord1937 = insert(ProgramSequenceChord.create(sequence1918, 24.000000, "C/G"));
    sequenceChord1938 = insert(ProgramSequenceChord.create(sequence1921, 14.000000, "D"));
    sequenceChord1939 = insert(ProgramSequenceChord.create(sequence1920, 0.000000, "A-"));
    sequenceChord1940 = insert(ProgramSequenceChord.create(sequence1919, 0.000000, "E-"));
    sequenceChord1941 = insert(ProgramSequenceChord.create(sequence1921, 4.000000, "C"));
    // Program Sequence Bindings
    sequenceBinding1942 = insert(ProgramSequenceBinding.create(sequence1919, 21));
    sequenceBinding1943 = insert(ProgramSequenceBinding.create(sequence1919, 22));
    sequenceBinding1944 = insert(ProgramSequenceBinding.create(sequence1921, 7));
    sequenceBinding1945 = insert(ProgramSequenceBinding.create(sequence1916, 13));
    sequenceBinding1946 = insert(ProgramSequenceBinding.create(sequence1916, 5));
    sequenceBinding1947 = insert(ProgramSequenceBinding.create(sequence1918, 18));
    sequenceBinding1948 = insert(ProgramSequenceBinding.create(sequence1921, 14));
    sequenceBinding1949 = insert(ProgramSequenceBinding.create(sequence1917, 21));
    sequenceBinding1950 = insert(ProgramSequenceBinding.create(sequence1917, 20));
    sequenceBinding1951 = insert(ProgramSequenceBinding.create(sequence1916, 3));
    sequenceBinding1952 = insert(ProgramSequenceBinding.create(sequence1921, 8));
    sequenceBinding1953 = insert(ProgramSequenceBinding.create(sequence1918, 19));
    sequenceBinding1954 = insert(ProgramSequenceBinding.create(sequence1920, 6));
    sequenceBinding1955 = insert(ProgramSequenceBinding.create(sequence1916, 4));
    sequenceBinding1956 = insert(ProgramSequenceBinding.create(sequence1917, 22));
    sequenceBinding1957 = insert(ProgramSequenceBinding.create(sequence1916, 12));
    sequenceBinding1958 = insert(ProgramSequenceBinding.create(sequence1919, 1));
    sequenceBinding1959 = insert(ProgramSequenceBinding.create(sequence1919, 20));
    sequenceBinding1960 = insert(ProgramSequenceBinding.create(sequence1917, 9));
    sequenceBinding1961 = insert(ProgramSequenceBinding.create(sequence1919, 0));
    sequenceBinding1962 = insert(ProgramSequenceBinding.create(sequence1919, 9));
    sequenceBinding1963 = insert(ProgramSequenceBinding.create(sequence1916, 11));
    sequenceBinding1964 = insert(ProgramSequenceBinding.create(sequence1917, 1));
    sequenceBinding1965 = insert(ProgramSequenceBinding.create(sequence1916, 10));
    sequenceBinding1966 = insert(ProgramSequenceBinding.create(sequence1917, 0));
    sequenceBinding1967 = insert(ProgramSequenceBinding.create(sequence1921, 15));
    sequenceBinding1968 = insert(ProgramSequenceBinding.create(sequence1918, 17));
    sequenceBinding1969 = insert(ProgramSequenceBinding.create(sequence1916, 2));
    sequenceBinding1970 = insert(ProgramSequenceBinding.create(sequence1918, 16));
    // Program Sequence Binding Memes
    sequenceBindingMeme1971 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1952, "Steady"));
    sequenceBindingMeme1972 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1942, "Steady"));
    sequenceBindingMeme1973 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1951, "Steady"));
    sequenceBindingMeme1974 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1957, "Steady"));
    sequenceBindingMeme1975 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1943, "Break"));
    sequenceBindingMeme1976 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1961, "Steady"));
    sequenceBindingMeme1977 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1958, "Break"));
    sequenceBindingMeme1978 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1962, "Break"));
    sequenceBindingMeme1979 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1968, "Steady"));
    sequenceBindingMeme1980 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1945, "Steady"));
    sequenceBindingMeme1981 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1950, "Break"));
    sequenceBindingMeme1982 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1965, "Steady"));
    sequenceBindingMeme1983 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1959, "Break"));
    sequenceBindingMeme1984 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1964, "Break"));
    sequenceBindingMeme1985 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1955, "Steady"));
    sequenceBindingMeme1986 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1967, "Break"));
    sequenceBindingMeme1987 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1963, "Steady"));
    sequenceBindingMeme1988 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1966, "Steady"));
    sequenceBindingMeme1989 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1969, "Steady"));
    sequenceBindingMeme1990 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1944, "Steady"));
    sequenceBindingMeme1991 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1948, "Steady"));
    sequenceBindingMeme1992 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1949, "Steady"));
    sequenceBindingMeme1993 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1953, "Steady"));
    sequenceBindingMeme1994 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1956, "Break"));
    sequenceBindingMeme1995 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1954, "Steady"));
    sequenceBindingMeme1996 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1947, "Steady"));
    sequenceBindingMeme1997 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1946, "Steady"));
    sequenceBindingMeme1998 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1970, "Steady"));
    sequenceBindingMeme1999 = insert(ProgramSequenceBindingMeme.create(sequenceBinding1960, "Break"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


  }

  private void go12() throws Exception {
    // Insert Main-type Program Fire Camp
    program35 = insert(Program.create(user27, library3, "Main", "Published", "Fire Camp", "C", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program35, "Fire"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2000 = insert(ProgramSequence.create(program35, 16, "A", 0.600000, "C", 130.000000));
    sequence2001 = insert(ProgramSequence.create(program35, 32, "B", 0.600000, "C", 130.000000));
    // Program Sequence Chords
    sequenceChord2002 = insert(ProgramSequenceChord.create(sequence2000, 11.500000, "Fmaj7"));
    sequenceChord2003 = insert(ProgramSequenceChord.create(sequence2001, 16.000000, "Dbmaj7"));
    sequenceChord2004 = insert(ProgramSequenceChord.create(sequence2000, 0.000000, "Cmaj7"));
    sequenceChord2005 = insert(ProgramSequenceChord.create(sequence2000, 3.500000, "Emaj7"));
    sequenceChord2006 = insert(ProgramSequenceChord.create(sequence2001, 24.000000, "Fmaj7"));
    sequenceChord2007 = insert(ProgramSequenceChord.create(sequence2000, 7.500000, "Abmaj7"));
    sequenceChord2008 = insert(ProgramSequenceChord.create(sequence2001, 3.500000, "Amaj7"));
    sequenceChord2009 = insert(ProgramSequenceChord.create(sequence2001, 19.500000, "Amaj7"));
    sequenceChord2010 = insert(ProgramSequenceChord.create(sequence2001, 0.000000, "Dbmaj7"));
    sequenceChord2011 = insert(ProgramSequenceChord.create(sequence2001, 8.000000, "Fmaj7"));
    // Program Sequence Bindings
    sequenceBinding2012 = insert(ProgramSequenceBinding.create(sequence2000, 1));
    sequenceBinding2013 = insert(ProgramSequenceBinding.create(sequence2001, 17));
    sequenceBinding2014 = insert(ProgramSequenceBinding.create(sequence2000, 7));
    sequenceBinding2015 = insert(ProgramSequenceBinding.create(sequence2001, 8));
    sequenceBinding2016 = insert(ProgramSequenceBinding.create(sequence2000, 11));
    sequenceBinding2017 = insert(ProgramSequenceBinding.create(sequence2000, 19));
    sequenceBinding2018 = insert(ProgramSequenceBinding.create(sequence2000, 15));
    sequenceBinding2019 = insert(ProgramSequenceBinding.create(sequence2001, 16));
    sequenceBinding2020 = insert(ProgramSequenceBinding.create(sequence2000, 18));
    sequenceBinding2021 = insert(ProgramSequenceBinding.create(sequence2000, 6));
    sequenceBinding2022 = insert(ProgramSequenceBinding.create(sequence2000, 12));
    sequenceBinding2023 = insert(ProgramSequenceBinding.create(sequence2000, 14));
    sequenceBinding2024 = insert(ProgramSequenceBinding.create(sequence2000, 0));
    sequenceBinding2025 = insert(ProgramSequenceBinding.create(sequence2000, 2));
    sequenceBinding2026 = insert(ProgramSequenceBinding.create(sequence2000, 13));
    sequenceBinding2027 = insert(ProgramSequenceBinding.create(sequence2001, 9));
    sequenceBinding2028 = insert(ProgramSequenceBinding.create(sequence2000, 4));
    sequenceBinding2029 = insert(ProgramSequenceBinding.create(sequence2000, 10));
    sequenceBinding2030 = insert(ProgramSequenceBinding.create(sequence2000, 21));
    sequenceBinding2031 = insert(ProgramSequenceBinding.create(sequence2000, 3));
    sequenceBinding2032 = insert(ProgramSequenceBinding.create(sequence2000, 5));
    sequenceBinding2033 = insert(ProgramSequenceBinding.create(sequence2000, 20));
    // Program Sequence Binding Memes
    sequenceBindingMeme2034 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2016, "Steady"));
    sequenceBindingMeme2035 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2014, "Steady"));
    sequenceBindingMeme2036 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2031, "Break"));
    sequenceBindingMeme2037 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2033, "Steady"));
    sequenceBindingMeme2038 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2018, "Steady"));
    sequenceBindingMeme2039 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2028, "Steady"));
    sequenceBindingMeme2040 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2022, "Steady"));
    sequenceBindingMeme2041 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2024, "Steady"));
    sequenceBindingMeme2042 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2017, "Steady"));
    sequenceBindingMeme2043 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2030, "Break"));
    sequenceBindingMeme2044 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2032, "Steady"));
    sequenceBindingMeme2045 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2027, "Break"));
    sequenceBindingMeme2046 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2025, "Steady"));
    sequenceBindingMeme2047 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2026, "Steady"));
    sequenceBindingMeme2048 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2013, "Break"));
    sequenceBindingMeme2049 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2015, "Steady"));
    sequenceBindingMeme2050 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2019, "Steady"));
    sequenceBindingMeme2051 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2021, "Steady"));
    sequenceBindingMeme2052 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2012, "Steady"));
    sequenceBindingMeme2053 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2020, "Steady"));
    sequenceBindingMeme2054 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2029, "Steady"));
    sequenceBindingMeme2055 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2023, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Fire Fire
    program64 = insert(Program.create(user27, library3, "Main", "Published", "Fire Fire", "C#-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program64, "Fire"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2056 = insert(ProgramSequence.create(program64, 32, "A", 0.600000, "C#-", 130.000000));
    // Program Sequence Chords
    sequenceChord2057 = insert(ProgramSequenceChord.create(sequence2056, 0.000000, "C#-"));
    sequenceChord2058 = insert(ProgramSequenceChord.create(sequence2056, 16.000000, "Emaj7/G#"));
    sequenceChord2059 = insert(ProgramSequenceChord.create(sequence2056, 8.000000, "C#-7/B"));
    sequenceChord2060 = insert(ProgramSequenceChord.create(sequence2056, 3.500000, "Emaj7add9"));
    sequenceChord2061 = insert(ProgramSequenceChord.create(sequence2056, 5.500000, "F#-6"));
    sequenceChord2062 = insert(ProgramSequenceChord.create(sequence2056, 17.500000, "E/A"));
    sequenceChord2063 = insert(ProgramSequenceChord.create(sequence2056, 19.500000, "E/B"));
    sequenceChord2064 = insert(ProgramSequenceChord.create(sequence2056, 1.500000, "C#sus4/D"));
    sequenceChord2065 = insert(ProgramSequenceChord.create(sequence2056, 21.500000, "Bmaj6"));
    sequenceChord2066 = insert(ProgramSequenceChord.create(sequence2056, 24.000000, "Badd4/C#"));
    // Program Sequence Bindings
    sequenceBinding2067 = insert(ProgramSequenceBinding.create(sequence2056, 7));
    sequenceBinding2068 = insert(ProgramSequenceBinding.create(sequence2056, 0));
    sequenceBinding2069 = insert(ProgramSequenceBinding.create(sequence2056, 4));
    sequenceBinding2070 = insert(ProgramSequenceBinding.create(sequence2056, 9));
    sequenceBinding2071 = insert(ProgramSequenceBinding.create(sequence2056, 3));
    sequenceBinding2072 = insert(ProgramSequenceBinding.create(sequence2056, 8));
    sequenceBinding2073 = insert(ProgramSequenceBinding.create(sequence2056, 2));
    sequenceBinding2074 = insert(ProgramSequenceBinding.create(sequence2056, 5));
    sequenceBinding2075 = insert(ProgramSequenceBinding.create(sequence2056, 6));
    sequenceBinding2076 = insert(ProgramSequenceBinding.create(sequence2056, 1));
    // Program Sequence Binding Memes
    sequenceBindingMeme2077 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2072, "Steady"));
    sequenceBindingMeme2078 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2071, "Steady"));
    sequenceBindingMeme2079 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2070, "Break"));
    sequenceBindingMeme2080 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2076, "Steady"));
    sequenceBindingMeme2081 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2068, "Steady"));
    sequenceBindingMeme2082 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2069, "Steady"));
    sequenceBindingMeme2083 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2067, "Steady"));
    sequenceBindingMeme2084 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2074, "Break"));
    sequenceBindingMeme2085 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2075, "Steady"));
    sequenceBindingMeme2086 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2073, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Fire NBD
    program80 = insert(Program.create(user27, library3, "Main", "Published", "Fire NBD", "Ab", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program80, "Fire"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2087 = insert(ProgramSequence.create(program80, 16, "A", 0.600000, "Ab", 130.000000));
    sequence2088 = insert(ProgramSequence.create(program80, 16, "0", 0.600000, "Ab", 130.000000));
    sequence2089 = insert(ProgramSequence.create(program80, 24, "00", 0.600000, "Ab", 130.000000));
    sequence2090 = insert(ProgramSequence.create(program80, 32, "000", 0.600000, "Ab", 130.000000));
    // Program Sequence Chords
    sequenceChord2091 = insert(ProgramSequenceChord.create(sequence2087, 0.000000, "Abmaj7"));
    sequenceChord2092 = insert(ProgramSequenceChord.create(sequence2088, 0.000000, "Dbmaj7"));
    sequenceChord2093 = insert(ProgramSequenceChord.create(sequence2090, 0.000000, "Dbmaj7"));
    sequenceChord2094 = insert(ProgramSequenceChord.create(sequence2089, 0.000000, "Dbmaj7"));
    sequenceChord2095 = insert(ProgramSequenceChord.create(sequence2087, 12.500000, "Amaj7"));
    sequenceChord2096 = insert(ProgramSequenceChord.create(sequence2087, 8.000000, "Emaj7"));
    sequenceChord2097 = insert(ProgramSequenceChord.create(sequence2087, 4.500000, "Bmaj7"));
    // Program Sequence Bindings
    sequenceBinding2098 = insert(ProgramSequenceBinding.create(sequence2087, 7));
    sequenceBinding2099 = insert(ProgramSequenceBinding.create(sequence2087, 15));
    sequenceBinding2100 = insert(ProgramSequenceBinding.create(sequence2088, 0));
    sequenceBinding2101 = insert(ProgramSequenceBinding.create(sequence2087, 5));
    sequenceBinding2102 = insert(ProgramSequenceBinding.create(sequence2087, 13));
    sequenceBinding2103 = insert(ProgramSequenceBinding.create(sequence2087, 4));
    sequenceBinding2104 = insert(ProgramSequenceBinding.create(sequence2087, 6));
    sequenceBinding2105 = insert(ProgramSequenceBinding.create(sequence2089, 0));
    sequenceBinding2106 = insert(ProgramSequenceBinding.create(sequence2089, 1));
    sequenceBinding2107 = insert(ProgramSequenceBinding.create(sequence2090, 11));
    sequenceBinding2108 = insert(ProgramSequenceBinding.create(sequence2087, 8));
    sequenceBinding2109 = insert(ProgramSequenceBinding.create(sequence2090, 0));
    sequenceBinding2110 = insert(ProgramSequenceBinding.create(sequence2090, 1));
    sequenceBinding2111 = insert(ProgramSequenceBinding.create(sequence2087, 9));
    sequenceBinding2112 = insert(ProgramSequenceBinding.create(sequence2088, 1));
    sequenceBinding2113 = insert(ProgramSequenceBinding.create(sequence2087, 3));
    sequenceBinding2114 = insert(ProgramSequenceBinding.create(sequence2090, 10));
    sequenceBinding2115 = insert(ProgramSequenceBinding.create(sequence2087, 2));
    sequenceBinding2116 = insert(ProgramSequenceBinding.create(sequence2087, 14));
    sequenceBinding2117 = insert(ProgramSequenceBinding.create(sequence2087, 12));
    // Program Sequence Binding Memes
    sequenceBindingMeme2118 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2114, "Steady"));
    sequenceBindingMeme2119 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2100, "Steady"));
    sequenceBindingMeme2120 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2105, "Steady"));
    sequenceBindingMeme2121 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2117, "Steady"));
    sequenceBindingMeme2122 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2101, "Steady"));
    sequenceBindingMeme2123 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2098, "Steady"));
    sequenceBindingMeme2124 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2107, "Steady"));
    sequenceBindingMeme2125 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2102, "Steady"));
    sequenceBindingMeme2126 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2104, "Steady"));
    sequenceBindingMeme2127 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2099, "Break"));
    sequenceBindingMeme2128 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2108, "Steady"));
    sequenceBindingMeme2129 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2116, "Steady"));
    sequenceBindingMeme2130 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2110, "Steady"));
    sequenceBindingMeme2131 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2109, "Steady"));
    sequenceBindingMeme2132 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2111, "Break"));
    sequenceBindingMeme2133 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2112, "Break"));
    sequenceBindingMeme2134 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2115, "Steady"));
    sequenceBindingMeme2135 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2113, "Steady"));
    sequenceBindingMeme2136 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2106, "Steady"));
    sequenceBindingMeme2137 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2103, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Fire Pal
    program86 = insert(Program.create(user27, library3, "Main", "Published", "Fire Pal", "Eb-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program86, "Fire"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2138 = insert(ProgramSequence.create(program86, 16, "0", 0.600000, "Eb-", 130.000000));
    sequence2139 = insert(ProgramSequence.create(program86, 32, "B", 0.600000, "Eb-", 130.000000));
    sequence2140 = insert(ProgramSequence.create(program86, 16, "A", 0.600000, "Eb-", 130.000000));
    // Program Sequence Chords
    sequenceChord2141 = insert(ProgramSequenceChord.create(sequence2139, 25.500000, "Emaj7add9"));
    sequenceChord2142 = insert(ProgramSequenceChord.create(sequence2140, 5.500000, "Gbmaj6"));
    sequenceChord2143 = insert(ProgramSequenceChord.create(sequence2139, 9.750000, "Emaj7"));
    sequenceChord2144 = insert(ProgramSequenceChord.create(sequence2138, 0.000000, "Eb5"));
    sequenceChord2145 = insert(ProgramSequenceChord.create(sequence2139, 8.000000, "G#-7"));
    sequenceChord2146 = insert(ProgramSequenceChord.create(sequence2139, 17.750000, "C#/B"));
    sequenceChord2147 = insert(ProgramSequenceChord.create(sequence2140, 0.000000, "Eb-7"));
    sequenceChord2148 = insert(ProgramSequenceChord.create(sequence2140, 8.000000, "Eb-7"));
    sequenceChord2149 = insert(ProgramSequenceChord.create(sequence2139, 1.750000, "C#/B"));
    sequenceChord2150 = insert(ProgramSequenceChord.create(sequence2139, 0.000000, "Bmaj7"));
    sequenceChord2151 = insert(ProgramSequenceChord.create(sequence2139, 16.000000, "Bmaj7"));
    sequenceChord2152 = insert(ProgramSequenceChord.create(sequence2139, 24.000000, "F#add9/A#"));
    // Program Sequence Bindings
    sequenceBinding2153 = insert(ProgramSequenceBinding.create(sequence2140, 6));
    sequenceBinding2154 = insert(ProgramSequenceBinding.create(sequence2138, 13));
    sequenceBinding2155 = insert(ProgramSequenceBinding.create(sequence2140, 9));
    sequenceBinding2156 = insert(ProgramSequenceBinding.create(sequence2140, 5));
    sequenceBinding2157 = insert(ProgramSequenceBinding.create(sequence2139, 20));
    sequenceBinding2158 = insert(ProgramSequenceBinding.create(sequence2140, 7));
    sequenceBinding2159 = insert(ProgramSequenceBinding.create(sequence2138, 2));
    sequenceBinding2160 = insert(ProgramSequenceBinding.create(sequence2139, 12));
    sequenceBinding2161 = insert(ProgramSequenceBinding.create(sequence2140, 17));
    sequenceBinding2162 = insert(ProgramSequenceBinding.create(sequence2138, 1));
    sequenceBinding2163 = insert(ProgramSequenceBinding.create(sequence2140, 11));
    sequenceBinding2164 = insert(ProgramSequenceBinding.create(sequence2138, 0));
    sequenceBinding2165 = insert(ProgramSequenceBinding.create(sequence2140, 16));
    sequenceBinding2166 = insert(ProgramSequenceBinding.create(sequence2138, 24));
    sequenceBinding2167 = insert(ProgramSequenceBinding.create(sequence2138, 14));
    sequenceBinding2168 = insert(ProgramSequenceBinding.create(sequence2139, 19));
    sequenceBinding2169 = insert(ProgramSequenceBinding.create(sequence2140, 8));
    sequenceBinding2170 = insert(ProgramSequenceBinding.create(sequence2138, 22));
    sequenceBinding2171 = insert(ProgramSequenceBinding.create(sequence2140, 18));
    sequenceBinding2172 = insert(ProgramSequenceBinding.create(sequence2138, 23));
    sequenceBinding2173 = insert(ProgramSequenceBinding.create(sequence2138, 3));
    sequenceBinding2174 = insert(ProgramSequenceBinding.create(sequence2140, 4));
    sequenceBinding2175 = insert(ProgramSequenceBinding.create(sequence2140, 15));
    sequenceBinding2176 = insert(ProgramSequenceBinding.create(sequence2138, 21));
    sequenceBinding2177 = insert(ProgramSequenceBinding.create(sequence2140, 10));
    // Program Sequence Binding Memes
    sequenceBindingMeme2178 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2171, "Break"));
    sequenceBindingMeme2179 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2172, "Steady"));
    sequenceBindingMeme2180 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2160, "Steady"));
    sequenceBindingMeme2181 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2166, "Break"));
    sequenceBindingMeme2182 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2164, "Steady"));
    sequenceBindingMeme2183 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2177, "Steady"));
    sequenceBindingMeme2184 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2167, "Break"));
    sequenceBindingMeme2185 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2162, "Steady"));
    sequenceBindingMeme2186 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2168, "Steady"));
    sequenceBindingMeme2187 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2155, "Steady"));
    sequenceBindingMeme2188 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2165, "Steady"));
    sequenceBindingMeme2189 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2163, "Break"));
    sequenceBindingMeme2190 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2154, "Steady"));
    sequenceBindingMeme2191 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2175, "Steady"));
    sequenceBindingMeme2192 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2170, "Steady"));
    sequenceBindingMeme2193 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2169, "Steady"));
    sequenceBindingMeme2194 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2174, "Steady"));
    sequenceBindingMeme2195 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2157, "Steady"));
    sequenceBindingMeme2196 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2161, "Steady"));
    sequenceBindingMeme2197 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2173, "Break"));
    sequenceBindingMeme2198 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2176, "Steady"));
    sequenceBindingMeme2199 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2156, "Steady"));
    sequenceBindingMeme2200 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2159, "Steady"));
    sequenceBindingMeme2201 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2153, "Steady"));
    sequenceBindingMeme2202 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2158, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Fire Tom Perez
    program60 = insert(Program.create(user27, library3, "Main", "Published", "Fire Tom Perez", "C-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program60, "Fire"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2203 = insert(ProgramSequence.create(program60, 16, "B", 0.600000, "C-", 130.000000));
    sequence2204 = insert(ProgramSequence.create(program60, 32, "A", 0.600000, "C-", 130.000000));
    // Program Sequence Chords
    sequenceChord2205 = insert(ProgramSequenceChord.create(sequence2204, 4.000000, "Db"));
    sequenceChord2206 = insert(ProgramSequenceChord.create(sequence2203, 4.000000, "B"));
    sequenceChord2207 = insert(ProgramSequenceChord.create(sequence2204, 29.500000, "E-"));
    sequenceChord2208 = insert(ProgramSequenceChord.create(sequence2204, 16.000000, "C-"));
    sequenceChord2209 = insert(ProgramSequenceChord.create(sequence2204, 0.000000, "C-"));
    sequenceChord2210 = insert(ProgramSequenceChord.create(sequence2203, 8.000000, "Eb-"));
    sequenceChord2211 = insert(ProgramSequenceChord.create(sequence2204, 12.000000, "Bb-"));
    sequenceChord2212 = insert(ProgramSequenceChord.create(sequence2204, 20.000000, "Db"));
    sequenceChord2213 = insert(ProgramSequenceChord.create(sequence2203, 0.000000, "Ab-"));
    // Program Sequence Bindings
    sequenceBinding2214 = insert(ProgramSequenceBinding.create(sequence2204, 2));
    sequenceBinding2215 = insert(ProgramSequenceBinding.create(sequence2203, 6));
    sequenceBinding2216 = insert(ProgramSequenceBinding.create(sequence2203, 7));
    sequenceBinding2217 = insert(ProgramSequenceBinding.create(sequence2203, 13));
    sequenceBinding2218 = insert(ProgramSequenceBinding.create(sequence2204, 10));
    sequenceBinding2219 = insert(ProgramSequenceBinding.create(sequence2203, 5));
    sequenceBinding2220 = insert(ProgramSequenceBinding.create(sequence2204, 8));
    sequenceBinding2221 = insert(ProgramSequenceBinding.create(sequence2204, 0));
    sequenceBinding2222 = insert(ProgramSequenceBinding.create(sequence2203, 16));
    sequenceBinding2223 = insert(ProgramSequenceBinding.create(sequence2204, 9));
    sequenceBinding2224 = insert(ProgramSequenceBinding.create(sequence2203, 12));
    sequenceBinding2225 = insert(ProgramSequenceBinding.create(sequence2203, 17));
    sequenceBinding2226 = insert(ProgramSequenceBinding.create(sequence2203, 15));
    sequenceBinding2227 = insert(ProgramSequenceBinding.create(sequence2203, 4));
    sequenceBinding2228 = insert(ProgramSequenceBinding.create(sequence2204, 1));
    sequenceBinding2229 = insert(ProgramSequenceBinding.create(sequence2203, 11));
    sequenceBinding2230 = insert(ProgramSequenceBinding.create(sequence2204, 3));
    sequenceBinding2231 = insert(ProgramSequenceBinding.create(sequence2203, 14));
    // Program Sequence Binding Memes
    sequenceBindingMeme2232 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2222, "Steady"));
    sequenceBindingMeme2233 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2221, "Steady"));
    sequenceBindingMeme2234 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2231, "Steady"));
    sequenceBindingMeme2235 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2216, "Break"));
    sequenceBindingMeme2236 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2224, "Steady"));
    sequenceBindingMeme2237 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2219, "Steady"));
    sequenceBindingMeme2238 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2214, "Steady"));
    sequenceBindingMeme2239 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2217, "Steady"));
    sequenceBindingMeme2240 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2215, "Steady"));
    sequenceBindingMeme2241 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2228, "Steady"));
    sequenceBindingMeme2242 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2226, "Steady"));
    sequenceBindingMeme2243 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2230, "Steady"));
    sequenceBindingMeme2244 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2218, "Steady"));
    sequenceBindingMeme2245 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2229, "Steady"));
    sequenceBindingMeme2246 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2227, "Steady"));
    sequenceBindingMeme2247 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2223, "Steady"));
    sequenceBindingMeme2248 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2225, "Break"));
    sequenceBindingMeme2249 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2220, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Fired
    program89 = insert(Program.create(user27, library3, "Main", "Published", "Fired", "A-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program89, "Fire"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2250 = insert(ProgramSequence.create(program89, 16, "NC", 0.600000, "A-", 130.000000));
    sequence2251 = insert(ProgramSequence.create(program89, 32, "A", 0.600000, "A-", 130.000000));
    sequence2252 = insert(ProgramSequence.create(program89, 16, "0", 0.600000, "A-", 130.000000));
    // Program Sequence Chords
    sequenceChord2253 = insert(ProgramSequenceChord.create(sequence2251, 31.500000, "A-7"));
    sequenceChord2254 = insert(ProgramSequenceChord.create(sequence2251, 19.500000, "E-7"));
    sequenceChord2255 = insert(ProgramSequenceChord.create(sequence2251, 28.500000, "C#dim7"));
    sequenceChord2256 = insert(ProgramSequenceChord.create(sequence2251, 29.500000, "D-7add9"));
    sequenceChord2257 = insert(ProgramSequenceChord.create(sequence2251, 0.000000, "A-7"));
    sequenceChord2258 = insert(ProgramSequenceChord.create(sequence2251, 7.500000, "Eb-7"));
    sequenceChord2259 = insert(ProgramSequenceChord.create(sequence2252, 0.000000, "A-7add9"));
    sequenceChord2260 = insert(ProgramSequenceChord.create(sequence2251, 16.000000, "A-7"));
    sequenceChord2261 = insert(ProgramSequenceChord.create(sequence2251, 3.500000, "F#-7"));
    sequenceChord2262 = insert(ProgramSequenceChord.create(sequence2251, 23.500000, "Fmaj7add9"));
    sequenceChord2263 = insert(ProgramSequenceChord.create(sequence2250, 0.000000, "NC"));
    // Program Sequence Bindings
    sequenceBinding2264 = insert(ProgramSequenceBinding.create(sequence2252, 10));
    sequenceBinding2265 = insert(ProgramSequenceBinding.create(sequence2250, 8));
    sequenceBinding2266 = insert(ProgramSequenceBinding.create(sequence2252, 9));
    sequenceBinding2267 = insert(ProgramSequenceBinding.create(sequence2251, 2));
    sequenceBinding2268 = insert(ProgramSequenceBinding.create(sequence2250, 15));
    sequenceBinding2269 = insert(ProgramSequenceBinding.create(sequence2251, 0));
    sequenceBinding2270 = insert(ProgramSequenceBinding.create(sequence2251, 11));
    sequenceBinding2271 = insert(ProgramSequenceBinding.create(sequence2251, 14));
    sequenceBinding2272 = insert(ProgramSequenceBinding.create(sequence2250, 16));
    sequenceBinding2273 = insert(ProgramSequenceBinding.create(sequence2251, 12));
    sequenceBinding2274 = insert(ProgramSequenceBinding.create(sequence2251, 3));
    sequenceBinding2275 = insert(ProgramSequenceBinding.create(sequence2252, 5));
    sequenceBinding2276 = insert(ProgramSequenceBinding.create(sequence2251, 4));
    sequenceBinding2277 = insert(ProgramSequenceBinding.create(sequence2251, 13));
    sequenceBinding2278 = insert(ProgramSequenceBinding.create(sequence2251, 1));
    sequenceBinding2279 = insert(ProgramSequenceBinding.create(sequence2250, 7));
    sequenceBinding2280 = insert(ProgramSequenceBinding.create(sequence2252, 6));
    // Program Sequence Binding Memes
    sequenceBindingMeme2281 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2269, "Steady"));
    sequenceBindingMeme2282 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2272, "Break"));
    sequenceBindingMeme2283 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2273, "Steady"));
    sequenceBindingMeme2284 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2278, "Steady"));
    sequenceBindingMeme2285 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2264, "Break"));
    sequenceBindingMeme2286 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2271, "Steady"));
    sequenceBindingMeme2287 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2280, "Break"));
    sequenceBindingMeme2288 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2275, "Steady"));
    sequenceBindingMeme2289 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2268, "Steady"));
    sequenceBindingMeme2290 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2274, "Steady"));
    sequenceBindingMeme2291 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2276, "Steady"));
    sequenceBindingMeme2292 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2267, "Steady"));
    sequenceBindingMeme2293 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2270, "Steady"));
    sequenceBindingMeme2294 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2279, "Steady"));
    sequenceBindingMeme2295 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2265, "Steady"));
    sequenceBindingMeme2296 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2266, "Steady"));
    sequenceBindingMeme2297 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2277, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Fireggio
    program76 = insert(Program.create(user27, library3, "Main", "Published", "Fireggio", "F#-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program76, "Fire"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2298 = insert(ProgramSequence.create(program76, 16, "A", 0.600000, "F#-", 130.000000));
    sequence2299 = insert(ProgramSequence.create(program76, 32, "F#-", 0.600000, "F#-", 130.000000));
    sequence2300 = insert(ProgramSequence.create(program76, 32, "0", 0.600000, "F#-", 130.000000));
    sequence2301 = insert(ProgramSequence.create(program76, 32, "F#5", 0.600000, "F#-", 130.000000));
    // Program Sequence Chords
    sequenceChord2302 = insert(ProgramSequenceChord.create(sequence2298, 11.500000, "B-7"));
    sequenceChord2303 = insert(ProgramSequenceChord.create(sequence2300, 0.000000, "NC"));
    sequenceChord2304 = insert(ProgramSequenceChord.create(sequence2299, 0.000000, "F#-"));
    sequenceChord2305 = insert(ProgramSequenceChord.create(sequence2298, 10.500000, "D"));
    sequenceChord2306 = insert(ProgramSequenceChord.create(sequence2301, 0.000000, "F#5"));
    sequenceChord2307 = insert(ProgramSequenceChord.create(sequence2298, 0.000000, "F#-"));
    sequenceChord2308 = insert(ProgramSequenceChord.create(sequence2298, 7.500000, "F#-"));
    sequenceChord2309 = insert(ProgramSequenceChord.create(sequence2298, 3.500000, "B-7"));
    sequenceChord2310 = insert(ProgramSequenceChord.create(sequence2298, 2.500000, "Gmaj7"));
    sequenceChord2311 = insert(ProgramSequenceChord.create(sequence2298, 6.500000, "C#-7"));
    sequenceChord2312 = insert(ProgramSequenceChord.create(sequence2298, 14.500000, "Eadd9"));
    // Program Sequence Bindings
    sequenceBinding2313 = insert(ProgramSequenceBinding.create(sequence2298, 15));
    sequenceBinding2314 = insert(ProgramSequenceBinding.create(sequence2298, 16));
    sequenceBinding2315 = insert(ProgramSequenceBinding.create(sequence2298, 17));
    sequenceBinding2316 = insert(ProgramSequenceBinding.create(sequence2299, 1));
    sequenceBinding2317 = insert(ProgramSequenceBinding.create(sequence2301, 1));
    sequenceBinding2318 = insert(ProgramSequenceBinding.create(sequence2298, 12));
    sequenceBinding2319 = insert(ProgramSequenceBinding.create(sequence2298, 4));
    sequenceBinding2320 = insert(ProgramSequenceBinding.create(sequence2300, 0));
    sequenceBinding2321 = insert(ProgramSequenceBinding.create(sequence2299, 11));
    sequenceBinding2322 = insert(ProgramSequenceBinding.create(sequence2298, 3));
    sequenceBinding2323 = insert(ProgramSequenceBinding.create(sequence2301, 10));
    sequenceBinding2324 = insert(ProgramSequenceBinding.create(sequence2298, 5));
    sequenceBinding2325 = insert(ProgramSequenceBinding.create(sequence2298, 8));
    sequenceBinding2326 = insert(ProgramSequenceBinding.create(sequence2298, 9));
    sequenceBinding2327 = insert(ProgramSequenceBinding.create(sequence2298, 14));
    sequenceBinding2328 = insert(ProgramSequenceBinding.create(sequence2299, 10));
    sequenceBinding2329 = insert(ProgramSequenceBinding.create(sequence2298, 6));
    sequenceBinding2330 = insert(ProgramSequenceBinding.create(sequence2298, 7));
    sequenceBinding2331 = insert(ProgramSequenceBinding.create(sequence2301, 11));
    sequenceBinding2332 = insert(ProgramSequenceBinding.create(sequence2300, 11));
    sequenceBinding2333 = insert(ProgramSequenceBinding.create(sequence2300, 1));
    sequenceBinding2334 = insert(ProgramSequenceBinding.create(sequence2298, 18));
    sequenceBinding2335 = insert(ProgramSequenceBinding.create(sequence2298, 19));
    sequenceBinding2336 = insert(ProgramSequenceBinding.create(sequence2298, 13));
    sequenceBinding2337 = insert(ProgramSequenceBinding.create(sequence2298, 2));
    sequenceBinding2338 = insert(ProgramSequenceBinding.create(sequence2300, 10));
    // Program Sequence Binding Memes
    sequenceBindingMeme2339 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2332, "Steady"));
    sequenceBindingMeme2340 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2336, "Steady"));
    sequenceBindingMeme2341 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2314, "Steady"));
    sequenceBindingMeme2342 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2321, "Steady"));
    sequenceBindingMeme2343 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2322, "Steady"));
    sequenceBindingMeme2344 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2323, "Steady"));
    sequenceBindingMeme2345 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2320, "Steady"));
    sequenceBindingMeme2346 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2326, "Break"));
    sequenceBindingMeme2347 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2319, "Steady"));
    sequenceBindingMeme2348 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2331, "Steady"));
    sequenceBindingMeme2349 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2338, "Steady"));
    sequenceBindingMeme2350 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2325, "Steady"));
    sequenceBindingMeme2351 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2335, "Break"));
    sequenceBindingMeme2352 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2333, "Steady"));
    sequenceBindingMeme2353 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2317, "Steady"));
    sequenceBindingMeme2354 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2324, "Steady"));
    sequenceBindingMeme2355 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2330, "Steady"));
    sequenceBindingMeme2356 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2315, "Steady"));
    sequenceBindingMeme2357 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2318, "Break"));
    sequenceBindingMeme2358 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2329, "Steady"));
    sequenceBindingMeme2359 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2334, "Steady"));
    sequenceBindingMeme2360 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2313, "Steady"));
    sequenceBindingMeme2361 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2337, "Break"));
    sequenceBindingMeme2362 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2316, "Steady"));
    sequenceBindingMeme2363 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2327, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program I'll House You
    program9 = insert(Program.create(user1, library1, "Main", "Published", "I'll House You", "C", 130.000000, 0.550000));
    // Program Memes
    insert(ProgramMeme.create(program9, "Deep"));
    insert(ProgramMeme.create(program9, "Hard"));
    insert(ProgramMeme.create(program9, "Classic"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2364 = insert(ProgramSequence.create(program9, 16, "Breakdown A", 0.600000, "G minor", 130.000000));
    sequence2365 = insert(ProgramSequence.create(program9, 32, "Drop", 0.400000, "C", 130.000000));
    sequence2366 = insert(ProgramSequence.create(program9, 16, "Breakdown B", 0.800000, "G minor", 130.000000));
    // Program Sequence Chords
    sequenceChord2367 = insert(ProgramSequenceChord.create(sequence2365, 12.000000, "F7"));
    sequenceChord2368 = insert(ProgramSequenceChord.create(sequence2365, 8.000000, "Cm7"));
    sequenceChord2369 = insert(ProgramSequenceChord.create(sequence2366, 0.000000, "E minor 7"));
    sequenceChord2370 = insert(ProgramSequenceChord.create(sequence2366, 8.000000, "D minor 7"));
    sequenceChord2371 = insert(ProgramSequenceChord.create(sequence2365, 16.000000, "Bb major 7"));
    sequenceChord2372 = insert(ProgramSequenceChord.create(sequence2364, 0.000000, "D"));
    sequenceChord2373 = insert(ProgramSequenceChord.create(sequence2366, 4.000000, "Eb minor 7"));
    sequenceChord2374 = insert(ProgramSequenceChord.create(sequence2365, 28.000000, "Eb7"));
    sequenceChord2375 = insert(ProgramSequenceChord.create(sequence2366, 12.000000, "Db minor 7"));
    sequenceChord2376 = insert(ProgramSequenceChord.create(sequence2364, 12.000000, "F7"));
    sequenceChord2377 = insert(ProgramSequenceChord.create(sequence2365, 0.000000, "C major 7"));
    sequenceChord2378 = insert(ProgramSequenceChord.create(sequence2365, 30.000000, "Ab major 7"));
    sequenceChord2379 = insert(ProgramSequenceChord.create(sequence2364, 4.000000, "G"));
    sequenceChord2380 = insert(ProgramSequenceChord.create(sequence2365, 24.000000, "Bb m7"));
    sequenceChord2381 = insert(ProgramSequenceChord.create(sequence2364, 8.000000, "C"));
    // Program Sequence Bindings
    sequenceBinding2382 = insert(ProgramSequenceBinding.create(sequence2365, 0));
    sequenceBinding2383 = insert(ProgramSequenceBinding.create(sequence2364, 1));
    sequenceBinding2384 = insert(ProgramSequenceBinding.create(sequence2366, 2));
    // Program Sequence Binding Memes
    sequenceBindingMeme2385 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2382, "Steady"));
    sequenceBindingMeme2386 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2383, "Steady"));
    sequenceBindingMeme2387 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2383, "Steady"));
    sequenceBindingMeme2388 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2384, "Hard"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events

  }


  private void go14() throws Exception {
    // Insert Main-type Program Water Bun
    program84 = insert(Program.create(user27, library3, "Main", "Published", "Water Bun", "B-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program84, "Water"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2389 = insert(ProgramSequence.create(program84, 32, "A", 0.600000, "B-", 130.000000));
    sequence2390 = insert(ProgramSequence.create(program84, 16, "0", 0.600000, "B-", 130.000000));
    // Program Sequence Chords
    sequenceChord2391 = insert(ProgramSequenceChord.create(sequence2389, 16.000000, "Gmaj7"));
    sequenceChord2392 = insert(ProgramSequenceChord.create(sequence2389, 0.000000, "B-7"));
    sequenceChord2393 = insert(ProgramSequenceChord.create(sequence2390, 0.000000, "B-"));
    sequenceChord2394 = insert(ProgramSequenceChord.create(sequence2389, 8.000000, "E-7"));
    sequenceChord2395 = insert(ProgramSequenceChord.create(sequence2389, 24.000000, "Cmaj7"));
    // Program Sequence Bindings
    sequenceBinding2396 = insert(ProgramSequenceBinding.create(sequence2389, 12));
    sequenceBinding2397 = insert(ProgramSequenceBinding.create(sequence2389, 3));
    sequenceBinding2398 = insert(ProgramSequenceBinding.create(sequence2389, 13));
    sequenceBinding2399 = insert(ProgramSequenceBinding.create(sequence2389, 14));
    sequenceBinding2400 = insert(ProgramSequenceBinding.create(sequence2389, 1));
    sequenceBinding2401 = insert(ProgramSequenceBinding.create(sequence2390, 10));
    sequenceBinding2402 = insert(ProgramSequenceBinding.create(sequence2389, 5));
    sequenceBinding2403 = insert(ProgramSequenceBinding.create(sequence2389, 4));
    sequenceBinding2404 = insert(ProgramSequenceBinding.create(sequence2390, 8));
    sequenceBinding2405 = insert(ProgramSequenceBinding.create(sequence2389, 17));
    sequenceBinding2406 = insert(ProgramSequenceBinding.create(sequence2389, 0));
    sequenceBinding2407 = insert(ProgramSequenceBinding.create(sequence2389, 2));
    sequenceBinding2408 = insert(ProgramSequenceBinding.create(sequence2389, 6));
    sequenceBinding2409 = insert(ProgramSequenceBinding.create(sequence2390, 9));
    sequenceBinding2410 = insert(ProgramSequenceBinding.create(sequence2389, 15));
    sequenceBinding2411 = insert(ProgramSequenceBinding.create(sequence2390, 11));
    sequenceBinding2412 = insert(ProgramSequenceBinding.create(sequence2389, 16));
    sequenceBinding2413 = insert(ProgramSequenceBinding.create(sequence2389, 7));
    // Program Sequence Binding Memes
    sequenceBindingMeme2414 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2402, "Steady"));
    sequenceBindingMeme2415 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2406, "Steady"));
    sequenceBindingMeme2416 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2403, "Steady"));
    sequenceBindingMeme2417 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2411, "Break"));
    sequenceBindingMeme2418 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2404, "Steady"));
    sequenceBindingMeme2419 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2407, "Steady"));
    sequenceBindingMeme2420 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2412, "Steady"));
    sequenceBindingMeme2421 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2408, "Steady"));
    sequenceBindingMeme2422 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2396, "Steady"));
    sequenceBindingMeme2423 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2397, "Steady"));
    sequenceBindingMeme2424 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2409, "Steady"));
    sequenceBindingMeme2425 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2410, "Steady"));
    sequenceBindingMeme2426 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2401, "Steady"));
    sequenceBindingMeme2427 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2399, "Steady"));
    sequenceBindingMeme2428 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2405, "Break"));
    sequenceBindingMeme2429 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2413, "Break"));
    sequenceBindingMeme2430 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2400, "Steady"));
    sequenceBindingMeme2431 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2398, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Water Galq
    program11 = insert(Program.create(user27, library3, "Main", "Published", "Water Galq", "E-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program11, "Water"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2432 = insert(ProgramSequence.create(program11, 12, "B", 0.600000, "E-", 130.000000));
    sequence2433 = insert(ProgramSequence.create(program11, 12, "A", 0.600000, "E-", 130.000000));
    sequence2434 = insert(ProgramSequence.create(program11, 16, "X", 0.600000, "E-", 130.000000));
    // Program Sequence Chords
    sequenceChord2435 = insert(ProgramSequenceChord.create(sequence2433, 4.000000, "Fmaj7"));
    sequenceChord2436 = insert(ProgramSequenceChord.create(sequence2433, 0.000000, "E-7"));
    sequenceChord2437 = insert(ProgramSequenceChord.create(sequence2432, 4.000000, "Cmaj7/E"));
    sequenceChord2438 = insert(ProgramSequenceChord.create(sequence2433, 8.000000, "A-7"));
    sequenceChord2439 = insert(ProgramSequenceChord.create(sequence2432, 0.000000, "Dmaj6"));
    sequenceChord2440 = insert(ProgramSequenceChord.create(sequence2432, 8.000000, "Cmaj7add9"));
    sequenceChord2441 = insert(ProgramSequenceChord.create(sequence2434, 8.000000, "C/G"));
    sequenceChord2442 = insert(ProgramSequenceChord.create(sequence2434, 0.000000, "D/G"));
    // Program Sequence Bindings
    sequenceBinding2443 = insert(ProgramSequenceBinding.create(sequence2433, 3));
    sequenceBinding2444 = insert(ProgramSequenceBinding.create(sequence2432, 25));
    sequenceBinding2445 = insert(ProgramSequenceBinding.create(sequence2433, 2));
    sequenceBinding2446 = insert(ProgramSequenceBinding.create(sequence2434, 1));
    sequenceBinding2447 = insert(ProgramSequenceBinding.create(sequence2433, 16));
    sequenceBinding2448 = insert(ProgramSequenceBinding.create(sequence2433, 4));
    sequenceBinding2449 = insert(ProgramSequenceBinding.create(sequence2432, 9));
    sequenceBinding2450 = insert(ProgramSequenceBinding.create(sequence2433, 18));
    sequenceBinding2451 = insert(ProgramSequenceBinding.create(sequence2432, 11));
    sequenceBinding2452 = insert(ProgramSequenceBinding.create(sequence2433, 19));
    sequenceBinding2453 = insert(ProgramSequenceBinding.create(sequence2434, 13));
    sequenceBinding2454 = insert(ProgramSequenceBinding.create(sequence2432, 24));
    sequenceBinding2455 = insert(ProgramSequenceBinding.create(sequence2433, 15));
    sequenceBinding2456 = insert(ProgramSequenceBinding.create(sequence2432, 10));
    sequenceBinding2457 = insert(ProgramSequenceBinding.create(sequence2433, 17));
    sequenceBinding2458 = insert(ProgramSequenceBinding.create(sequence2432, 8));
    sequenceBinding2459 = insert(ProgramSequenceBinding.create(sequence2432, 20));
    sequenceBinding2460 = insert(ProgramSequenceBinding.create(sequence2432, 22));
    sequenceBinding2461 = insert(ProgramSequenceBinding.create(sequence2432, 26));
    sequenceBinding2462 = insert(ProgramSequenceBinding.create(sequence2433, 14));
    sequenceBinding2463 = insert(ProgramSequenceBinding.create(sequence2433, 6));
    sequenceBinding2464 = insert(ProgramSequenceBinding.create(sequence2433, 5));
    sequenceBinding2465 = insert(ProgramSequenceBinding.create(sequence2433, 7));
    sequenceBinding2466 = insert(ProgramSequenceBinding.create(sequence2434, 12));
    sequenceBinding2467 = insert(ProgramSequenceBinding.create(sequence2434, 0));
    sequenceBinding2468 = insert(ProgramSequenceBinding.create(sequence2432, 23));
    sequenceBinding2469 = insert(ProgramSequenceBinding.create(sequence2432, 21));
    // Program Sequence Binding Memes
    sequenceBindingMeme2470 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2449, "Steady"));
    sequenceBindingMeme2471 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2462, "Steady"));
    sequenceBindingMeme2472 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2444, "Steady"));
    sequenceBindingMeme2473 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2461, "Break"));
    sequenceBindingMeme2474 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2456, "Steady"));
    sequenceBindingMeme2475 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2445, "Steady"));
    sequenceBindingMeme2476 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2446, "Steady"));
    sequenceBindingMeme2477 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2451, "Steady"));
    sequenceBindingMeme2478 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2454, "Steady"));
    sequenceBindingMeme2479 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2450, "Steady"));
    sequenceBindingMeme2480 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2460, "Steady"));
    sequenceBindingMeme2481 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2455, "Steady"));
    sequenceBindingMeme2482 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2458, "Steady"));
    sequenceBindingMeme2483 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2467, "Steady"));
    sequenceBindingMeme2484 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2469, "Steady"));
    sequenceBindingMeme2485 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2448, "Steady"));
    sequenceBindingMeme2486 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2459, "Steady"));
    sequenceBindingMeme2487 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2464, "Steady"));
    sequenceBindingMeme2488 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2466, "Steady"));
    sequenceBindingMeme2489 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2453, "Break"));
    sequenceBindingMeme2490 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2447, "Steady"));
    sequenceBindingMeme2491 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2463, "Steady"));
    sequenceBindingMeme2492 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2465, "Break"));
    sequenceBindingMeme2493 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2452, "Break"));
    sequenceBindingMeme2494 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2443, "Steady"));
    sequenceBindingMeme2495 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2468, "Steady"));
    sequenceBindingMeme2496 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2457, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Water Me Up
    program49 = insert(Program.create(user27, library3, "Main", "Published", "Water Me Up", "F", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program49, "Water"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2497 = insert(ProgramSequence.create(program49, 32, "B", 0.600000, "F", 130.000000));
    sequence2498 = insert(ProgramSequence.create(program49, 8, "C", 0.600000, "F", 130.000000));
    sequence2499 = insert(ProgramSequence.create(program49, 16, "A", 0.600000, "F", 130.000000));
    // Program Sequence Chords
    sequenceChord2500 = insert(ProgramSequenceChord.create(sequence2499, 0.000000, "Fmaj7"));
    sequenceChord2501 = insert(ProgramSequenceChord.create(sequence2498, 11.500000, "Gbmaj7"));
    sequenceChord2502 = insert(ProgramSequenceChord.create(sequence2497, 20.000000, "Gbmaj7"));
    sequenceChord2503 = insert(ProgramSequenceChord.create(sequence2497, 0.000000, "Bb-7"));
    sequenceChord2504 = insert(ProgramSequenceChord.create(sequence2498, 0.000000, "Fmaj7"));
    sequenceChord2505 = insert(ProgramSequenceChord.create(sequence2498, 7.500000, "Bb-7"));
    sequenceChord2506 = insert(ProgramSequenceChord.create(sequence2497, 16.000000, "Bb-7"));
    sequenceChord2507 = insert(ProgramSequenceChord.create(sequence2497, 28.000000, "Gbmaj7/Ab"));
    sequenceChord2508 = insert(ProgramSequenceChord.create(sequence2497, 4.000000, "Gbmaj7"));
    sequenceChord2509 = insert(ProgramSequenceChord.create(sequence2498, 3.500000, "Dbmaj7"));
    sequenceChord2510 = insert(ProgramSequenceChord.create(sequence2499, 4.000000, "Ebmaj7"));
    sequenceChord2511 = insert(ProgramSequenceChord.create(sequence2497, 12.000000, "Eb-7"));
    // Program Sequence Bindings
    sequenceBinding2512 = insert(ProgramSequenceBinding.create(sequence2498, 24));
    sequenceBinding2513 = insert(ProgramSequenceBinding.create(sequence2498, 23));
    sequenceBinding2514 = insert(ProgramSequenceBinding.create(sequence2498, 25));
    sequenceBinding2515 = insert(ProgramSequenceBinding.create(sequence2497, 9));
    sequenceBinding2516 = insert(ProgramSequenceBinding.create(sequence2499, 3));
    sequenceBinding2517 = insert(ProgramSequenceBinding.create(sequence2499, 6));
    sequenceBinding2518 = insert(ProgramSequenceBinding.create(sequence2499, 0));
    sequenceBinding2519 = insert(ProgramSequenceBinding.create(sequence2499, 1));
    sequenceBinding2520 = insert(ProgramSequenceBinding.create(sequence2499, 13));
    sequenceBinding2521 = insert(ProgramSequenceBinding.create(sequence2499, 10));
    sequenceBinding2522 = insert(ProgramSequenceBinding.create(sequence2499, 7));
    sequenceBinding2523 = insert(ProgramSequenceBinding.create(sequence2498, 19));
    sequenceBinding2524 = insert(ProgramSequenceBinding.create(sequence2499, 14));
    sequenceBinding2525 = insert(ProgramSequenceBinding.create(sequence2499, 12));
    sequenceBinding2526 = insert(ProgramSequenceBinding.create(sequence2497, 8));
    sequenceBinding2527 = insert(ProgramSequenceBinding.create(sequence2499, 2));
    sequenceBinding2528 = insert(ProgramSequenceBinding.create(sequence2498, 18));
    sequenceBinding2529 = insert(ProgramSequenceBinding.create(sequence2499, 15));
    sequenceBinding2530 = insert(ProgramSequenceBinding.create(sequence2498, 21));
    sequenceBinding2531 = insert(ProgramSequenceBinding.create(sequence2499, 5));
    sequenceBinding2532 = insert(ProgramSequenceBinding.create(sequence2498, 20));
    sequenceBinding2533 = insert(ProgramSequenceBinding.create(sequence2499, 11));
    sequenceBinding2534 = insert(ProgramSequenceBinding.create(sequence2497, 16));
    sequenceBinding2535 = insert(ProgramSequenceBinding.create(sequence2497, 17));
    sequenceBinding2536 = insert(ProgramSequenceBinding.create(sequence2499, 4));
    sequenceBinding2537 = insert(ProgramSequenceBinding.create(sequence2498, 22));
    // Program Sequence Binding Memes
    sequenceBindingMeme2538 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2518, "Steady"));
    sequenceBindingMeme2539 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2513, "Break"));
    sequenceBindingMeme2540 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2523, "Break"));
    sequenceBindingMeme2541 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2537, "Break"));
    sequenceBindingMeme2542 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2519, "Steady"));
    sequenceBindingMeme2543 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2534, "Steady"));
    sequenceBindingMeme2544 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2535, "Steady"));
    sequenceBindingMeme2545 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2516, "Steady"));
    sequenceBindingMeme2546 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2521, "Steady"));
    sequenceBindingMeme2547 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2533, "Steady"));
    sequenceBindingMeme2548 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2517, "Steady"));
    sequenceBindingMeme2549 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2530, "Break"));
    sequenceBindingMeme2550 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2514, "Break"));
    sequenceBindingMeme2551 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2512, "Break"));
    sequenceBindingMeme2552 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2532, "Break"));
    sequenceBindingMeme2553 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2525, "Steady"));
    sequenceBindingMeme2554 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2520, "Steady"));
    sequenceBindingMeme2555 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2526, "Steady"));
    sequenceBindingMeme2556 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2529, "Steady"));
    sequenceBindingMeme2557 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2536, "Steady"));
    sequenceBindingMeme2558 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2522, "Steady"));
    sequenceBindingMeme2559 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2515, "Break"));
    sequenceBindingMeme2560 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2528, "Break"));
    sequenceBindingMeme2561 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2527, "Steady"));
    sequenceBindingMeme2562 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2531, "Steady"));
    sequenceBindingMeme2563 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2524, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Water Probe
    program87 = insert(Program.create(user27, library3, "Main", "Published", "Water Probe", "A-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program87, "Water"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2564 = insert(ProgramSequence.create(program87, 16, "A", 0.600000, "A-", 130.000000));
    sequence2565 = insert(ProgramSequence.create(program87, 16, "0", 0.600000, "A-", 130.000000));
    sequence2566 = insert(ProgramSequence.create(program87, 16, "B", 0.600000, "A-", 130.000000));
    // Program Sequence Chords
    sequenceChord2567 = insert(ProgramSequenceChord.create(sequence2566, 11.000000, "Fmaj6/G"));
    sequenceChord2568 = insert(ProgramSequenceChord.create(sequence2564, 3.000000, "Bbmaj7"));
    sequenceChord2569 = insert(ProgramSequenceChord.create(sequence2564, 14.000000, "Gmaj6"));
    sequenceChord2570 = insert(ProgramSequenceChord.create(sequence2564, 14.000000, "Fmaj7"));
    sequenceChord2571 = insert(ProgramSequenceChord.create(sequence2565, 0.000000, "E-7/A"));
    sequenceChord2572 = insert(ProgramSequenceChord.create(sequence2566, 3.000000, "Esus4/F"));
    sequenceChord2573 = insert(ProgramSequenceChord.create(sequence2566, 8.000000, "Csus2/E"));
    sequenceChord2574 = insert(ProgramSequenceChord.create(sequence2564, 0.000000, "A-7"));
    sequenceChord2575 = insert(ProgramSequenceChord.create(sequence2566, 0.000000, "Esus4"));
    sequenceChord2576 = insert(ProgramSequenceChord.create(sequence2564, 11.000000, "Fmaj7"));
    // Program Sequence Bindings
    sequenceBinding2577 = insert(ProgramSequenceBinding.create(sequence2564, 7));
    sequenceBinding2578 = insert(ProgramSequenceBinding.create(sequence2566, 19));
    sequenceBinding2579 = insert(ProgramSequenceBinding.create(sequence2565, 0));
    sequenceBinding2580 = insert(ProgramSequenceBinding.create(sequence2564, 8));
    sequenceBinding2581 = insert(ProgramSequenceBinding.create(sequence2566, 23));
    sequenceBinding2582 = insert(ProgramSequenceBinding.create(sequence2566, 25));
    sequenceBinding2583 = insert(ProgramSequenceBinding.create(sequence2564, 4));
    sequenceBinding2584 = insert(ProgramSequenceBinding.create(sequence2564, 6));
    sequenceBinding2585 = insert(ProgramSequenceBinding.create(sequence2565, 13));
    sequenceBinding2586 = insert(ProgramSequenceBinding.create(sequence2564, 14));
    sequenceBinding2587 = insert(ProgramSequenceBinding.create(sequence2566, 22));
    sequenceBinding2588 = insert(ProgramSequenceBinding.create(sequence2564, 5));
    sequenceBinding2589 = insert(ProgramSequenceBinding.create(sequence2565, 11));
    sequenceBinding2590 = insert(ProgramSequenceBinding.create(sequence2565, 12));
    sequenceBinding2591 = insert(ProgramSequenceBinding.create(sequence2565, 10));
    sequenceBinding2592 = insert(ProgramSequenceBinding.create(sequence2566, 18));
    sequenceBinding2593 = insert(ProgramSequenceBinding.create(sequence2565, 1));
    sequenceBinding2594 = insert(ProgramSequenceBinding.create(sequence2564, 3));
    sequenceBinding2595 = insert(ProgramSequenceBinding.create(sequence2564, 15));
    sequenceBinding2596 = insert(ProgramSequenceBinding.create(sequence2564, 2));
    sequenceBinding2597 = insert(ProgramSequenceBinding.create(sequence2564, 17));
    sequenceBinding2598 = insert(ProgramSequenceBinding.create(sequence2564, 16));
    sequenceBinding2599 = insert(ProgramSequenceBinding.create(sequence2566, 21));
    sequenceBinding2600 = insert(ProgramSequenceBinding.create(sequence2564, 9));
    sequenceBinding2601 = insert(ProgramSequenceBinding.create(sequence2566, 20));
    sequenceBinding2602 = insert(ProgramSequenceBinding.create(sequence2566, 24));
    // Program Sequence Binding Memes
    sequenceBindingMeme2603 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2577, "Steady"));
    sequenceBindingMeme2604 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2585, "Steady"));
    sequenceBindingMeme2605 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2586, "Steady"));
    sequenceBindingMeme2606 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2593, "Steady"));
    sequenceBindingMeme2607 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2600, "Break"));
    sequenceBindingMeme2608 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2582, "Break"));
    sequenceBindingMeme2609 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2602, "Steady"));
    sequenceBindingMeme2610 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2596, "Steady"));
    sequenceBindingMeme2611 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2579, "Steady"));
    sequenceBindingMeme2612 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2598, "Steady"));
    sequenceBindingMeme2613 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2592, "Steady"));
    sequenceBindingMeme2614 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2589, "Steady"));
    sequenceBindingMeme2615 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2599, "Steady"));
    sequenceBindingMeme2616 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2595, "Steady"));
    sequenceBindingMeme2617 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2580, "Steady"));
    sequenceBindingMeme2618 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2587, "Steady"));
    sequenceBindingMeme2619 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2583, "Steady"));
    sequenceBindingMeme2620 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2601, "Steady"));
    sequenceBindingMeme2621 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2578, "Steady"));
    sequenceBindingMeme2622 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2581, "Steady"));
    sequenceBindingMeme2623 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2590, "Steady"));
    sequenceBindingMeme2624 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2597, "Break"));
    sequenceBindingMeme2625 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2588, "Steady"));
    sequenceBindingMeme2626 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2591, "Steady"));
    sequenceBindingMeme2627 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2594, "Steady"));
    sequenceBindingMeme2628 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2584, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Water Rotcher
    program82 = insert(Program.create(user27, library3, "Main", "Published", "Water Rotcher", "F#-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program82, "Water"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2629 = insert(ProgramSequence.create(program82, 16, "I", 0.600000, "F#-", 130.000000));
    sequence2630 = insert(ProgramSequence.create(program82, 16, "A", 0.600000, "F#-", 130.000000));
    sequence2631 = insert(ProgramSequence.create(program82, 64, "0", 0.600000, "F#-", 130.000000));
    // Program Sequence Chords
    sequenceChord2632 = insert(ProgramSequenceChord.create(sequence2630, 4.000000, "Aadd9/F#"));
    sequenceChord2633 = insert(ProgramSequenceChord.create(sequence2631, 0.000000, "NC"));
    sequenceChord2634 = insert(ProgramSequenceChord.create(sequence2630, 0.000000, "Dmaj7"));
    sequenceChord2635 = insert(ProgramSequenceChord.create(sequence2630, 8.000000, "F/Bb"));
    sequenceChord2636 = insert(ProgramSequenceChord.create(sequence2629, 0.000000, "F#5"));
    sequenceChord2637 = insert(ProgramSequenceChord.create(sequence2630, 12.000000, "G/C"));
    // Program Sequence Bindings
    sequenceBinding2638 = insert(ProgramSequenceBinding.create(sequence2630, 7));
    sequenceBinding2639 = insert(ProgramSequenceBinding.create(sequence2630, 3));
    sequenceBinding2640 = insert(ProgramSequenceBinding.create(sequence2630, 6));
    sequenceBinding2641 = insert(ProgramSequenceBinding.create(sequence2630, 8));
    sequenceBinding2642 = insert(ProgramSequenceBinding.create(sequence2630, 17));
    sequenceBinding2643 = insert(ProgramSequenceBinding.create(sequence2630, 9));
    sequenceBinding2644 = insert(ProgramSequenceBinding.create(sequence2630, 19));
    sequenceBinding2645 = insert(ProgramSequenceBinding.create(sequence2630, 2));
    sequenceBinding2646 = insert(ProgramSequenceBinding.create(sequence2629, 15));
    sequenceBinding2647 = insert(ProgramSequenceBinding.create(sequence2629, 11));
    sequenceBinding2648 = insert(ProgramSequenceBinding.create(sequence2630, 20));
    sequenceBinding2649 = insert(ProgramSequenceBinding.create(sequence2630, 16));
    sequenceBinding2650 = insert(ProgramSequenceBinding.create(sequence2630, 10));
    sequenceBinding2651 = insert(ProgramSequenceBinding.create(sequence2630, 5));
    sequenceBinding2652 = insert(ProgramSequenceBinding.create(sequence2631, 13));
    sequenceBinding2653 = insert(ProgramSequenceBinding.create(sequence2630, 21));
    sequenceBinding2654 = insert(ProgramSequenceBinding.create(sequence2629, 1));
    sequenceBinding2655 = insert(ProgramSequenceBinding.create(sequence2629, 0));
    sequenceBinding2656 = insert(ProgramSequenceBinding.create(sequence2630, 4));
    sequenceBinding2657 = insert(ProgramSequenceBinding.create(sequence2629, 12));
    sequenceBinding2658 = insert(ProgramSequenceBinding.create(sequence2630, 18));
    sequenceBinding2659 = insert(ProgramSequenceBinding.create(sequence2629, 14));
    // Program Sequence Binding Memes
    sequenceBindingMeme2660 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2649, "Steady"));
    sequenceBindingMeme2661 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2652, "Steady"));
    sequenceBindingMeme2662 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2648, "Steady"));
    sequenceBindingMeme2663 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2641, "Steady"));
    sequenceBindingMeme2664 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2657, "Break"));
    sequenceBindingMeme2665 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2644, "Steady"));
    sequenceBindingMeme2666 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2659, "Steady"));
    sequenceBindingMeme2667 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2654, "Steady"));
    sequenceBindingMeme2668 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2658, "Steady"));
    sequenceBindingMeme2669 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2639, "Steady"));
    sequenceBindingMeme2670 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2647, "Steady"));
    sequenceBindingMeme2671 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2645, "Steady"));
    sequenceBindingMeme2672 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2650, "Steady"));
    sequenceBindingMeme2673 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2656, "Steady"));
    sequenceBindingMeme2674 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2640, "Steady"));
    sequenceBindingMeme2675 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2646, "Break"));
    sequenceBindingMeme2676 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2642, "Steady"));
    sequenceBindingMeme2677 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2651, "Steady"));
    sequenceBindingMeme2678 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2653, "Break"));
    sequenceBindingMeme2679 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2643, "Steady"));
    sequenceBindingMeme2680 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2638, "Steady"));
    sequenceBindingMeme2681 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2655, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Water Water
    program65 = insert(Program.create(user27, library3, "Main", "Published", "Water Water", "C#", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program65, "Water"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2682 = insert(ProgramSequence.create(program65, 32, "A", 0.600000, "C#", 130.000000));
    // Program Sequence Chords
    sequenceChord2683 = insert(ProgramSequenceChord.create(sequence2682, 0.000000, "C#maj7"));
    sequenceChord2684 = insert(ProgramSequenceChord.create(sequence2682, 9.500000, "Emaj7add9"));
    sequenceChord2685 = insert(ProgramSequenceChord.create(sequence2682, 8.000000, "G#-7"));
    sequenceChord2686 = insert(ProgramSequenceChord.create(sequence2682, 1.500000, "F#-7add9"));
    sequenceChord2687 = insert(ProgramSequenceChord.create(sequence2682, 17.500000, "Amaj7"));
    sequenceChord2688 = insert(ProgramSequenceChord.create(sequence2682, 16.000000, "C#maj7"));
    sequenceChord2689 = insert(ProgramSequenceChord.create(sequence2682, 24.000000, "G#-7"));
    sequenceChord2690 = insert(ProgramSequenceChord.create(sequence2682, 25.500000, "Dmaj7add13"));
    // Program Sequence Bindings
    sequenceBinding2691 = insert(ProgramSequenceBinding.create(sequence2682, 2));
    sequenceBinding2692 = insert(ProgramSequenceBinding.create(sequence2682, 5));
    sequenceBinding2693 = insert(ProgramSequenceBinding.create(sequence2682, 4));
    sequenceBinding2694 = insert(ProgramSequenceBinding.create(sequence2682, 1));
    sequenceBinding2695 = insert(ProgramSequenceBinding.create(sequence2682, 11));
    sequenceBinding2696 = insert(ProgramSequenceBinding.create(sequence2682, 12));
    sequenceBinding2697 = insert(ProgramSequenceBinding.create(sequence2682, 13));
    sequenceBinding2698 = insert(ProgramSequenceBinding.create(sequence2682, 3));
    sequenceBinding2699 = insert(ProgramSequenceBinding.create(sequence2682, 10));
    sequenceBinding2700 = insert(ProgramSequenceBinding.create(sequence2682, 7));
    sequenceBinding2701 = insert(ProgramSequenceBinding.create(sequence2682, 9));
    sequenceBinding2702 = insert(ProgramSequenceBinding.create(sequence2682, 6));
    sequenceBinding2703 = insert(ProgramSequenceBinding.create(sequence2682, 8));
    sequenceBinding2704 = insert(ProgramSequenceBinding.create(sequence2682, 0));
    // Program Sequence Binding Memes
    sequenceBindingMeme2705 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2703, "Steady"));
    sequenceBindingMeme2706 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2696, "Steady"));
    sequenceBindingMeme2707 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2697, "Break"));
    sequenceBindingMeme2708 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2692, "Steady"));
    sequenceBindingMeme2709 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2695, "Steady"));
    sequenceBindingMeme2710 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2700, "Break"));
    sequenceBindingMeme2711 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2694, "Steady"));
    sequenceBindingMeme2712 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2701, "Steady"));
    sequenceBindingMeme2713 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2693, "Steady"));
    sequenceBindingMeme2714 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2699, "Steady"));
    sequenceBindingMeme2715 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2702, "Steady"));
    sequenceBindingMeme2716 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2698, "Steady"));
    sequenceBindingMeme2717 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2704, "Steady"));
    sequenceBindingMeme2718 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2691, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Water Wibs
    program57 = insert(Program.create(user27, library3, "Main", "Published", "Water Wibs", "C#-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program57, "Water"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2719 = insert(ProgramSequence.create(program57, 16, "A", 0.600000, "C#-", 130.000000));
    sequence2720 = insert(ProgramSequence.create(program57, 16, "B", 0.600000, "C#-", 130.000000));
    // Program Sequence Chords
    sequenceChord2721 = insert(ProgramSequenceChord.create(sequence2719, 0.000000, "C#-7"));
    sequenceChord2722 = insert(ProgramSequenceChord.create(sequence2720, 3.750000, "Amaj7/E"));
    sequenceChord2723 = insert(ProgramSequenceChord.create(sequence2719, 14.500000, "D#-7"));
    sequenceChord2724 = insert(ProgramSequenceChord.create(sequence2720, 0.000000, "G#-"));
    sequenceChord2725 = insert(ProgramSequenceChord.create(sequence2720, 11.750000, "Bmaj6"));
    // Program Sequence Bindings
    sequenceBinding2726 = insert(ProgramSequenceBinding.create(sequence2719, 0));
    sequenceBinding2727 = insert(ProgramSequenceBinding.create(sequence2719, 18));
    sequenceBinding2728 = insert(ProgramSequenceBinding.create(sequence2719, 11));
    sequenceBinding2729 = insert(ProgramSequenceBinding.create(sequence2720, 12));
    sequenceBinding2730 = insert(ProgramSequenceBinding.create(sequence2719, 3));
    sequenceBinding2731 = insert(ProgramSequenceBinding.create(sequence2719, 16));
    sequenceBinding2732 = insert(ProgramSequenceBinding.create(sequence2720, 14));
    sequenceBinding2733 = insert(ProgramSequenceBinding.create(sequence2719, 8));
    sequenceBinding2734 = insert(ProgramSequenceBinding.create(sequence2719, 1));
    sequenceBinding2735 = insert(ProgramSequenceBinding.create(sequence2719, 10));
    sequenceBinding2736 = insert(ProgramSequenceBinding.create(sequence2720, 15));
    sequenceBinding2737 = insert(ProgramSequenceBinding.create(sequence2719, 19));
    sequenceBinding2738 = insert(ProgramSequenceBinding.create(sequence2719, 17));
    sequenceBinding2739 = insert(ProgramSequenceBinding.create(sequence2720, 4));
    sequenceBinding2740 = insert(ProgramSequenceBinding.create(sequence2719, 2));
    sequenceBinding2741 = insert(ProgramSequenceBinding.create(sequence2720, 7));
    sequenceBinding2742 = insert(ProgramSequenceBinding.create(sequence2720, 5));
    sequenceBinding2743 = insert(ProgramSequenceBinding.create(sequence2720, 6));
    sequenceBinding2744 = insert(ProgramSequenceBinding.create(sequence2719, 9));
    sequenceBinding2745 = insert(ProgramSequenceBinding.create(sequence2720, 13));
    // Program Sequence Binding Memes
    sequenceBindingMeme2746 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2726, "Steady"));
    sequenceBindingMeme2747 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2727, "Steady"));
    sequenceBindingMeme2748 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2738, "Steady"));
    sequenceBindingMeme2749 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2743, "Steady"));
    sequenceBindingMeme2750 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2737, "Break"));
    sequenceBindingMeme2751 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2734, "Steady"));
    sequenceBindingMeme2752 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2739, "Steady"));
    sequenceBindingMeme2753 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2735, "Steady"));
    sequenceBindingMeme2754 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2742, "Steady"));
    sequenceBindingMeme2755 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2745, "Steady"));
    sequenceBindingMeme2756 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2731, "Steady"));
    sequenceBindingMeme2757 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2740, "Steady"));
    sequenceBindingMeme2758 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2728, "Break"));
    sequenceBindingMeme2759 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2736, "Break"));
    sequenceBindingMeme2760 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2730, "Steady"));
    sequenceBindingMeme2761 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2741, "Steady"));
    sequenceBindingMeme2762 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2732, "Steady"));
    sequenceBindingMeme2763 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2733, "Steady"));
    sequenceBindingMeme2764 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2744, "Steady"));
    sequenceBindingMeme2765 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2729, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Waternity
    program77 = insert(Program.create(user27, library3, "Main", "Published", "Waternity", "C", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program77, "Water"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2766 = insert(ProgramSequence.create(program77, 32, "A", 0.600000, "C", 130.000000));
    // Program Sequence Chords
    sequenceChord2767 = insert(ProgramSequenceChord.create(sequence2766, 16.000000, "D-/C"));
    sequenceChord2768 = insert(ProgramSequenceChord.create(sequence2766, 0.000000, "C"));
    sequenceChord2769 = insert(ProgramSequenceChord.create(sequence2766, 8.000000, "F/C"));
    sequenceChord2770 = insert(ProgramSequenceChord.create(sequence2766, 24.000000, "F/C"));
    // Program Sequence Bindings
    sequenceBinding2771 = insert(ProgramSequenceBinding.create(sequence2766, 8));
    sequenceBinding2772 = insert(ProgramSequenceBinding.create(sequence2766, 5));
    sequenceBinding2773 = insert(ProgramSequenceBinding.create(sequence2766, 11));
    sequenceBinding2774 = insert(ProgramSequenceBinding.create(sequence2766, 7));
    sequenceBinding2775 = insert(ProgramSequenceBinding.create(sequence2766, 2));
    sequenceBinding2776 = insert(ProgramSequenceBinding.create(sequence2766, 6));
    sequenceBinding2777 = insert(ProgramSequenceBinding.create(sequence2766, 9));
    sequenceBinding2778 = insert(ProgramSequenceBinding.create(sequence2766, 4));
    sequenceBinding2779 = insert(ProgramSequenceBinding.create(sequence2766, 10));
    sequenceBinding2780 = insert(ProgramSequenceBinding.create(sequence2766, 0));
    sequenceBinding2781 = insert(ProgramSequenceBinding.create(sequence2766, 3));
    sequenceBinding2782 = insert(ProgramSequenceBinding.create(sequence2766, 1));
    // Program Sequence Binding Memes
    sequenceBindingMeme2783 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2781, "Steady"));
    sequenceBindingMeme2784 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2773, "Break"));
    sequenceBindingMeme2785 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2779, "Steady"));
    sequenceBindingMeme2786 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2775, "Steady"));
    sequenceBindingMeme2787 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2772, "Steady"));
    sequenceBindingMeme2788 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2776, "Steady"));
    sequenceBindingMeme2789 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2782, "Steady"));
    sequenceBindingMeme2790 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2771, "Steady"));
    sequenceBindingMeme2791 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2774, "Steady"));
    sequenceBindingMeme2792 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2780, "Steady"));
    sequenceBindingMeme2793 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2778, "Steady"));
    sequenceBindingMeme2794 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2777, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events
  }

  private void go15() throws Exception {
    // Insert Main-type Program Wind 88
    program85 = insert(Program.create(user27, library3, "Main", "Published", "Wind 88", "F-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program85, "Wind"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2795 = insert(ProgramSequence.create(program85, 16, "0", 0.600000, "F-", 130.000000));
    sequence2796 = insert(ProgramSequence.create(program85, 32, "A", 0.600000, "F-", 130.000000));
    sequence2797 = insert(ProgramSequence.create(program85, 24, "00", 0.600000, "F-", 130.000000));
    sequence2798 = insert(ProgramSequence.create(program85, 32, "000", 0.600000, "F-", 130.000000));
    // Program Sequence Chords
    sequenceChord2799 = insert(ProgramSequenceChord.create(sequence2795, 0.000000, "F-7"));
    sequenceChord2800 = insert(ProgramSequenceChord.create(sequence2797, 0.000000, "F-7"));
    sequenceChord2801 = insert(ProgramSequenceChord.create(sequence2796, 3.500000, "G-7"));
    sequenceChord2802 = insert(ProgramSequenceChord.create(sequence2796, 8.000000, "Bb-7"));
    sequenceChord2803 = insert(ProgramSequenceChord.create(sequence2796, 16.000000, "F-7"));
    sequenceChord2804 = insert(ProgramSequenceChord.create(sequence2796, 0.000000, "F-"));
    sequenceChord2805 = insert(ProgramSequenceChord.create(sequence2796, 11.500000, "Emaj7"));
    sequenceChord2806 = insert(ProgramSequenceChord.create(sequence2798, 0.000000, "F-7"));
    // Program Sequence Bindings
    sequenceBinding2807 = insert(ProgramSequenceBinding.create(sequence2796, 1));
    sequenceBinding2808 = insert(ProgramSequenceBinding.create(sequence2797, 0));
    sequenceBinding2809 = insert(ProgramSequenceBinding.create(sequence2796, 4));
    sequenceBinding2810 = insert(ProgramSequenceBinding.create(sequence2795, 8));
    sequenceBinding2811 = insert(ProgramSequenceBinding.create(sequence2798, 8));
    sequenceBinding2812 = insert(ProgramSequenceBinding.create(sequence2796, 9));
    sequenceBinding2813 = insert(ProgramSequenceBinding.create(sequence2797, 8));
    sequenceBinding2814 = insert(ProgramSequenceBinding.create(sequence2796, 10));
    sequenceBinding2815 = insert(ProgramSequenceBinding.create(sequence2796, 11));
    sequenceBinding2816 = insert(ProgramSequenceBinding.create(sequence2795, 7));
    sequenceBinding2817 = insert(ProgramSequenceBinding.create(sequence2796, 6));
    sequenceBinding2818 = insert(ProgramSequenceBinding.create(sequence2796, 5));
    sequenceBinding2819 = insert(ProgramSequenceBinding.create(sequence2796, 2));
    sequenceBinding2820 = insert(ProgramSequenceBinding.create(sequence2796, 12));
    sequenceBinding2821 = insert(ProgramSequenceBinding.create(sequence2797, 7));
    sequenceBinding2822 = insert(ProgramSequenceBinding.create(sequence2795, 0));
    sequenceBinding2823 = insert(ProgramSequenceBinding.create(sequence2798, 7));
    sequenceBinding2824 = insert(ProgramSequenceBinding.create(sequence2798, 0));
    sequenceBinding2825 = insert(ProgramSequenceBinding.create(sequence2796, 3));
    // Program Sequence Binding Memes
    sequenceBindingMeme2826 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2816, "Steady"));
    sequenceBindingMeme2827 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2817, "Steady"));
    sequenceBindingMeme2828 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2812, "Steady"));
    sequenceBindingMeme2829 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2809, "Steady"));
    sequenceBindingMeme2830 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2819, "Steady"));
    sequenceBindingMeme2831 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2807, "Steady"));
    sequenceBindingMeme2832 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2820, "Break"));
    sequenceBindingMeme2833 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2822, "Break"));
    sequenceBindingMeme2834 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2808, "Steady"));
    sequenceBindingMeme2835 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2810, "Break"));
    sequenceBindingMeme2836 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2824, "Steady"));
    sequenceBindingMeme2837 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2818, "Steady"));
    sequenceBindingMeme2838 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2821, "Steady"));
    sequenceBindingMeme2839 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2811, "Steady"));
    sequenceBindingMeme2840 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2815, "Steady"));
    sequenceBindingMeme2841 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2823, "Steady"));
    sequenceBindingMeme2842 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2825, "Steady"));
    sequenceBindingMeme2843 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2813, "Steady"));
    sequenceBindingMeme2844 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2814, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Wind Bagz
    program53 = insert(Program.create(user27, library3, "Main", "Published", "Wind Bagz", "Eb", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program53, "Wind"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2845 = insert(ProgramSequence.create(program53, 32, "A", 0.600000, "Eb", 130.000000));
    sequence2846 = insert(ProgramSequence.create(program53, 16, "I", 0.600000, "Eb", 130.000000));
    sequence2847 = insert(ProgramSequence.create(program53, 32, "II", 0.600000, "Eb", 130.000000));
    sequence2848 = insert(ProgramSequence.create(program53, 32, "B", 0.600000, "Eb", 130.000000));
    // Program Sequence Chords
    sequenceChord2849 = insert(ProgramSequenceChord.create(sequence2845, 29.500000, "Abmaj7"));
    sequenceChord2850 = insert(ProgramSequenceChord.create(sequence2848, 24.000000, "Bb7sus4"));
    sequenceChord2851 = insert(ProgramSequenceChord.create(sequence2846, 13.500000, "G-7/C"));
    sequenceChord2852 = insert(ProgramSequenceChord.create(sequence2847, 16.000000, "Abmaj7/Bb"));
    sequenceChord2853 = insert(ProgramSequenceChord.create(sequence2848, 8.000000, "F-7"));
    sequenceChord2854 = insert(ProgramSequenceChord.create(sequence2845, 0.000000, "Ebmaj7"));
    sequenceChord2855 = insert(ProgramSequenceChord.create(sequence2847, 29.500000, "G-7/C"));
    sequenceChord2856 = insert(ProgramSequenceChord.create(sequence2845, 24.000000, "G-7"));
    sequenceChord2857 = insert(ProgramSequenceChord.create(sequence2847, 13.500000, "G-7/C"));
    sequenceChord2858 = insert(ProgramSequenceChord.create(sequence2846, 0.000000, "Abmaj7/Bb"));
    sequenceChord2859 = insert(ProgramSequenceChord.create(sequence2848, 0.000000, "Abmaj7"));
    sequenceChord2860 = insert(ProgramSequenceChord.create(sequence2845, 16.000000, "Ebmaj7"));
    sequenceChord2861 = insert(ProgramSequenceChord.create(sequence2848, 16.000000, "C-"));
    sequenceChord2862 = insert(ProgramSequenceChord.create(sequence2845, 27.500000, "F-7"));
    sequenceChord2863 = insert(ProgramSequenceChord.create(sequence2845, 8.000000, "G-7"));
    sequenceChord2864 = insert(ProgramSequenceChord.create(sequence2847, 0.000000, "Abmaj7/Bb"));
    // Program Sequence Bindings
    sequenceBinding2865 = insert(ProgramSequenceBinding.create(sequence2845, 2));
    sequenceBinding2866 = insert(ProgramSequenceBinding.create(sequence2846, 18));
    sequenceBinding2867 = insert(ProgramSequenceBinding.create(sequence2845, 23));
    sequenceBinding2868 = insert(ProgramSequenceBinding.create(sequence2847, 16));
    sequenceBinding2869 = insert(ProgramSequenceBinding.create(sequence2847, 18));
    sequenceBinding2870 = insert(ProgramSequenceBinding.create(sequence2846, 19));
    sequenceBinding2871 = insert(ProgramSequenceBinding.create(sequence2845, 8));
    sequenceBinding2872 = insert(ProgramSequenceBinding.create(sequence2848, 13));
    sequenceBinding2873 = insert(ProgramSequenceBinding.create(sequence2846, 17));
    sequenceBinding2874 = insert(ProgramSequenceBinding.create(sequence2845, 20));
    sequenceBinding2875 = insert(ProgramSequenceBinding.create(sequence2845, 5));
    sequenceBinding2876 = insert(ProgramSequenceBinding.create(sequence2847, 1));
    sequenceBinding2877 = insert(ProgramSequenceBinding.create(sequence2848, 6));
    sequenceBinding2878 = insert(ProgramSequenceBinding.create(sequence2845, 22));
    sequenceBinding2879 = insert(ProgramSequenceBinding.create(sequence2845, 9));
    sequenceBinding2880 = insert(ProgramSequenceBinding.create(sequence2848, 7));
    sequenceBinding2881 = insert(ProgramSequenceBinding.create(sequence2848, 12));
    sequenceBinding2882 = insert(ProgramSequenceBinding.create(sequence2845, 11));
    sequenceBinding2883 = insert(ProgramSequenceBinding.create(sequence2845, 21));
    sequenceBinding2884 = insert(ProgramSequenceBinding.create(sequence2846, 0));
    sequenceBinding2885 = insert(ProgramSequenceBinding.create(sequence2846, 1));
    sequenceBinding2886 = insert(ProgramSequenceBinding.create(sequence2848, 14));
    sequenceBinding2887 = insert(ProgramSequenceBinding.create(sequence2847, 0));
    sequenceBinding2888 = insert(ProgramSequenceBinding.create(sequence2848, 15));
    sequenceBinding2889 = insert(ProgramSequenceBinding.create(sequence2845, 4));
    sequenceBinding2890 = insert(ProgramSequenceBinding.create(sequence2845, 10));
    sequenceBinding2891 = insert(ProgramSequenceBinding.create(sequence2847, 17));
    sequenceBinding2892 = insert(ProgramSequenceBinding.create(sequence2846, 16));
    sequenceBinding2893 = insert(ProgramSequenceBinding.create(sequence2845, 3));
    // Program Sequence Binding Memes
    sequenceBindingMeme2894 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2889, "Steady"));
    sequenceBindingMeme2895 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2869, "Steady"));
    sequenceBindingMeme2896 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2883, "Steady"));
    sequenceBindingMeme2897 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2872, "Steady"));
    sequenceBindingMeme2898 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2878, "Steady"));
    sequenceBindingMeme2899 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2893, "Steady"));
    sequenceBindingMeme2900 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2870, "Break"));
    sequenceBindingMeme2901 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2871, "Steady"));
    sequenceBindingMeme2902 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2868, "Steady"));
    sequenceBindingMeme2903 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2866, "Steady"));
    sequenceBindingMeme2904 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2880, "Steady"));
    sequenceBindingMeme2905 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2885, "Steady"));
    sequenceBindingMeme2906 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2882, "Steady"));
    sequenceBindingMeme2907 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2881, "Steady"));
    sequenceBindingMeme2908 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2887, "Steady"));
    sequenceBindingMeme2909 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2876, "Steady"));
    sequenceBindingMeme2910 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2891, "Steady"));
    sequenceBindingMeme2911 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2886, "Steady"));
    sequenceBindingMeme2912 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2865, "Steady"));
    sequenceBindingMeme2913 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2884, "Steady"));
    sequenceBindingMeme2914 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2890, "Steady"));
    sequenceBindingMeme2915 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2867, "Break"));
    sequenceBindingMeme2916 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2874, "Steady"));
    sequenceBindingMeme2917 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2873, "Break"));
    sequenceBindingMeme2918 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2888, "Steady"));
    sequenceBindingMeme2919 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2875, "Steady"));
    sequenceBindingMeme2920 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2877, "Steady"));
    sequenceBindingMeme2921 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2892, "Steady"));
    sequenceBindingMeme2922 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2879, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Wind Mole
    program54 = insert(Program.create(user27, library3, "Main", "Published", "Wind Mole", "F", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program54, "Wind"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2923 = insert(ProgramSequence.create(program54, 16, "A", 0.600000, "F", 130.000000));
    sequence2924 = insert(ProgramSequence.create(program54, 16, "B", 0.600000, "F", 130.000000));
    // Program Sequence Chords
    sequenceChord2925 = insert(ProgramSequenceChord.create(sequence2923, 0.000000, "Fsus4add3"));
    sequenceChord2926 = insert(ProgramSequenceChord.create(sequence2924, 8.000000, "Fsus4/Gb"));
    sequenceChord2927 = insert(ProgramSequenceChord.create(sequence2923, 8.000000, "C7sus4"));
    sequenceChord2928 = insert(ProgramSequenceChord.create(sequence2924, 0.000000, "Ebmaj6/9"));
    // Program Sequence Bindings
    sequenceBinding2929 = insert(ProgramSequenceBinding.create(sequence2924, 10));
    sequenceBinding2930 = insert(ProgramSequenceBinding.create(sequence2923, 4));
    sequenceBinding2931 = insert(ProgramSequenceBinding.create(sequence2924, 23));
    sequenceBinding2932 = insert(ProgramSequenceBinding.create(sequence2923, 24));
    sequenceBinding2933 = insert(ProgramSequenceBinding.create(sequence2924, 22));
    sequenceBinding2934 = insert(ProgramSequenceBinding.create(sequence2923, 7));
    sequenceBinding2935 = insert(ProgramSequenceBinding.create(sequence2923, 2));
    sequenceBinding2936 = insert(ProgramSequenceBinding.create(sequence2924, 8));
    sequenceBinding2937 = insert(ProgramSequenceBinding.create(sequence2924, 19));
    sequenceBinding2938 = insert(ProgramSequenceBinding.create(sequence2924, 9));
    sequenceBinding2939 = insert(ProgramSequenceBinding.create(sequence2923, 25));
    sequenceBinding2940 = insert(ProgramSequenceBinding.create(sequence2923, 6));
    sequenceBinding2941 = insert(ProgramSequenceBinding.create(sequence2924, 21));
    sequenceBinding2942 = insert(ProgramSequenceBinding.create(sequence2923, 14));
    sequenceBinding2943 = insert(ProgramSequenceBinding.create(sequence2923, 16));
    sequenceBinding2944 = insert(ProgramSequenceBinding.create(sequence2923, 17));
    sequenceBinding2945 = insert(ProgramSequenceBinding.create(sequence2924, 11));
    sequenceBinding2946 = insert(ProgramSequenceBinding.create(sequence2923, 5));
    sequenceBinding2947 = insert(ProgramSequenceBinding.create(sequence2923, 15));
    sequenceBinding2948 = insert(ProgramSequenceBinding.create(sequence2924, 18));
    sequenceBinding2949 = insert(ProgramSequenceBinding.create(sequence2923, 3));
    sequenceBinding2950 = insert(ProgramSequenceBinding.create(sequence2923, 0));
    sequenceBinding2951 = insert(ProgramSequenceBinding.create(sequence2923, 12));
    sequenceBinding2952 = insert(ProgramSequenceBinding.create(sequence2923, 1));
    sequenceBinding2953 = insert(ProgramSequenceBinding.create(sequence2923, 13));
    sequenceBinding2954 = insert(ProgramSequenceBinding.create(sequence2923, 4));
    sequenceBinding2955 = insert(ProgramSequenceBinding.create(sequence2924, 20));
    // Program Sequence Binding Memes
    sequenceBindingMeme2956 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2945, "Steady"));
    sequenceBindingMeme2957 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2942, "Steady"));
    sequenceBindingMeme2958 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2946, "Steady"));
    sequenceBindingMeme2959 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2939, "Break"));
    sequenceBindingMeme2960 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2937, "Steady"));
    sequenceBindingMeme2961 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2929, "Steady"));
    sequenceBindingMeme2962 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2932, "Steady"));
    sequenceBindingMeme2963 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2944, "Break"));
    sequenceBindingMeme2964 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2933, "Steady"));
    sequenceBindingMeme2965 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2935, "Steady"));
    sequenceBindingMeme2966 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2941, "Steady"));
    sequenceBindingMeme2967 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2953, "Steady"));
    sequenceBindingMeme2968 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2949, "Steady"));
    sequenceBindingMeme2969 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2931, "Steady"));
    sequenceBindingMeme2970 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2940, "Steady"));
    sequenceBindingMeme2971 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2930, "Steady"));
    sequenceBindingMeme2972 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2954, "Steady"));
    sequenceBindingMeme2973 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2950, "Steady"));
    sequenceBindingMeme2974 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2947, "Steady"));
    sequenceBindingMeme2975 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2951, "Steady"));
    sequenceBindingMeme2976 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2936, "Steady"));
    sequenceBindingMeme2977 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2938, "Steady"));
    sequenceBindingMeme2978 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2948, "Steady"));
    sequenceBindingMeme2979 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2943, "Steady"));
    sequenceBindingMeme2980 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2934, "Break"));
    sequenceBindingMeme2981 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2955, "Steady"));
    sequenceBindingMeme2982 = insert(ProgramSequenceBindingMeme.create(sequenceBinding2952, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Wind Terb
    program50 = insert(Program.create(user27, library3, "Main", "Published", "Wind Terb", "D-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program50, "Wind"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence2983 = insert(ProgramSequence.create(program50, 32, "II", 0.600000, "D-", 130.000000));
    sequence2984 = insert(ProgramSequence.create(program50, 16, "A", 0.600000, "D-", 130.000000));
    sequence2985 = insert(ProgramSequence.create(program50, 32, "B", 0.600000, "D-", 130.000000));
    sequence2986 = insert(ProgramSequence.create(program50, 16, "I", 0.600000, "D-", 130.000000));
    sequence2987 = insert(ProgramSequence.create(program50, 64, "IIII", 0.600000, "D-", 130.000000));
    sequence2988 = insert(ProgramSequence.create(program50, 48, "III", 0.600000, "D-", 130.000000));
    // Program Sequence Chords
    sequenceChord2989 = insert(ProgramSequenceChord.create(sequence2987, 0.000000, "NC"));
    sequenceChord2990 = insert(ProgramSequenceChord.create(sequence2988, 0.000000, "NC"));
    sequenceChord2991 = insert(ProgramSequenceChord.create(sequence2985, 0.000000, "D-/C"));
    sequenceChord2992 = insert(ProgramSequenceChord.create(sequence2985, 16.000000, "D-/F"));
    sequenceChord2993 = insert(ProgramSequenceChord.create(sequence2983, 0.000000, "NC"));
    sequenceChord2994 = insert(ProgramSequenceChord.create(sequence2985, 8.000000, "Bbmaj7"));
    sequenceChord2995 = insert(ProgramSequenceChord.create(sequence2985, 24.000000, "D-/G"));
    sequenceChord2996 = insert(ProgramSequenceChord.create(sequence2984, 3.000000, "D-/G"));
    sequenceChord2997 = insert(ProgramSequenceChord.create(sequence2986, 0.000000, "NC"));
    sequenceChord2998 = insert(ProgramSequenceChord.create(sequence2984, 11.000000, "D-/F"));
    sequenceChord2999 = insert(ProgramSequenceChord.create(sequence2984, 0.000000, "D-"));
    // Program Sequence Bindings
    sequenceBinding3000 = insert(ProgramSequenceBinding.create(sequence2984, 5));
    sequenceBinding3001 = insert(ProgramSequenceBinding.create(sequence2985, 16));
    sequenceBinding3002 = insert(ProgramSequenceBinding.create(sequence2984, 12));
    sequenceBinding3003 = insert(ProgramSequenceBinding.create(sequence2984, 11));
    sequenceBinding3004 = insert(ProgramSequenceBinding.create(sequence2983, 14));
    sequenceBinding3005 = insert(ProgramSequenceBinding.create(sequence2984, 4));
    sequenceBinding3006 = insert(ProgramSequenceBinding.create(sequence2986, 0));
    sequenceBinding3007 = insert(ProgramSequenceBinding.create(sequence2986, 14));
    sequenceBinding3008 = insert(ProgramSequenceBinding.create(sequence2983, 13));
    sequenceBinding3009 = insert(ProgramSequenceBinding.create(sequence2984, 7));
    sequenceBinding3010 = insert(ProgramSequenceBinding.create(sequence2984, 18));
    sequenceBinding3011 = insert(ProgramSequenceBinding.create(sequence2984, 19));
    sequenceBinding3012 = insert(ProgramSequenceBinding.create(sequence2988, 13));
    sequenceBinding3013 = insert(ProgramSequenceBinding.create(sequence2984, 3));
    sequenceBinding3014 = insert(ProgramSequenceBinding.create(sequence2984, 8));
    sequenceBinding3015 = insert(ProgramSequenceBinding.create(sequence2984, 20));
    sequenceBinding3016 = insert(ProgramSequenceBinding.create(sequence2984, 2));
    sequenceBinding3017 = insert(ProgramSequenceBinding.create(sequence2985, 15));
    sequenceBinding3018 = insert(ProgramSequenceBinding.create(sequence2988, 14));
    sequenceBinding3019 = insert(ProgramSequenceBinding.create(sequence2983, 0));
    sequenceBinding3020 = insert(ProgramSequenceBinding.create(sequence2985, 9));
    sequenceBinding3021 = insert(ProgramSequenceBinding.create(sequence2985, 17));
    sequenceBinding3022 = insert(ProgramSequenceBinding.create(sequence2984, 1));
    sequenceBinding3023 = insert(ProgramSequenceBinding.create(sequence2987, 21));
    sequenceBinding3024 = insert(ProgramSequenceBinding.create(sequence2986, 13));
    sequenceBinding3025 = insert(ProgramSequenceBinding.create(sequence2988, 0));
    sequenceBinding3026 = insert(ProgramSequenceBinding.create(sequence2985, 10));
    sequenceBinding3027 = insert(ProgramSequenceBinding.create(sequence2984, 6));
    sequenceBinding3028 = insert(ProgramSequenceBinding.create(sequence2987, 13));
    sequenceBinding3029 = insert(ProgramSequenceBinding.create(sequence2987, 0));
    // Program Sequence Binding Memes
    sequenceBindingMeme3030 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3014, "Break"));
    sequenceBindingMeme3031 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3001, "Steady"));
    sequenceBindingMeme3032 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3018, "Steady"));
    sequenceBindingMeme3033 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3025, "Steady"));
    sequenceBindingMeme3034 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3026, "Steady"));
    sequenceBindingMeme3035 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3006, "Break"));
    sequenceBindingMeme3036 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3015, "Steady"));
    sequenceBindingMeme3037 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3004, "Steady"));
    sequenceBindingMeme3038 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3003, "Steady"));
    sequenceBindingMeme3039 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3020, "Steady"));
    sequenceBindingMeme3040 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3000, "Steady"));
    sequenceBindingMeme3041 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3005, "Steady"));
    sequenceBindingMeme3042 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3002, "Steady"));
    sequenceBindingMeme3043 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3011, "Steady"));
    sequenceBindingMeme3044 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3027, "Steady"));
    sequenceBindingMeme3045 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3010, "Steady"));
    sequenceBindingMeme3046 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3019, "Steady"));
    sequenceBindingMeme3047 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3024, "Break"));
    sequenceBindingMeme3048 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3028, "Steady"));
    sequenceBindingMeme3049 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3017, "Steady"));
    sequenceBindingMeme3050 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3012, "Steady"));
    sequenceBindingMeme3051 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3029, "Steady"));
    sequenceBindingMeme3052 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3016, "Steady"));
    sequenceBindingMeme3053 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3023, "Break"));
    sequenceBindingMeme3054 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3009, "Steady"));
    sequenceBindingMeme3055 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3022, "Steady"));
    sequenceBindingMeme3056 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3021, "Steady"));
    sequenceBindingMeme3057 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3007, "Break"));
    sequenceBindingMeme3058 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3008, "Steady"));
    sequenceBindingMeme3059 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3013, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Wind Wind
    program62 = insert(Program.create(user27, library3, "Main", "Published", "Wind Wind", "F", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program62, "Wind"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence3060 = insert(ProgramSequence.create(program62, 64, "A", 0.600000, "F", 130.000000));
    // Program Sequence Chords
    sequenceChord3061 = insert(ProgramSequenceChord.create(sequence3060, 16.000000, "F5/D"));
    sequenceChord3062 = insert(ProgramSequenceChord.create(sequence3060, 60.000000, "C7sus4"));
    sequenceChord3063 = insert(ProgramSequenceChord.create(sequence3060, 0.000000, "F5"));
    sequenceChord3064 = insert(ProgramSequenceChord.create(sequence3060, 32.000000, "F5/Db"));
    sequenceChord3065 = insert(ProgramSequenceChord.create(sequence3060, 48.000000, "Abmaj6"));
    // Program Sequence Bindings
    sequenceBinding3066 = insert(ProgramSequenceBinding.create(sequence3060, 5));
    sequenceBinding3067 = insert(ProgramSequenceBinding.create(sequence3060, 1));
    sequenceBinding3068 = insert(ProgramSequenceBinding.create(sequence3060, 3));
    sequenceBinding3069 = insert(ProgramSequenceBinding.create(sequence3060, 4));
    sequenceBinding3070 = insert(ProgramSequenceBinding.create(sequence3060, 0));
    sequenceBinding3071 = insert(ProgramSequenceBinding.create(sequence3060, 2));
    sequenceBinding3072 = insert(ProgramSequenceBinding.create(sequence3060, 7));
    sequenceBinding3073 = insert(ProgramSequenceBinding.create(sequence3060, 6));
    // Program Sequence Binding Memes
    sequenceBindingMeme3074 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3072, "Break"));
    sequenceBindingMeme3075 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3071, "Steady"));
    sequenceBindingMeme3076 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3073, "Steady"));
    sequenceBindingMeme3077 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3067, "Steady"));
    sequenceBindingMeme3078 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3070, "Steady"));
    sequenceBindingMeme3079 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3069, "Steady"));
    sequenceBindingMeme3080 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3066, "Steady"));
    sequenceBindingMeme3081 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3068, "Break"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Wind X
    program83 = insert(Program.create(user27, library3, "Main", "Published", "Wind X", "C", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program83, "Wind"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence3082 = insert(ProgramSequence.create(program83, 32, "A", 0.600000, "C", 130.000000));
    // Program Sequence Chords
    sequenceChord3083 = insert(ProgramSequenceChord.create(sequence3082, 0.000000, "NC"));
    // Program Sequence Bindings
    sequenceBinding3084 = insert(ProgramSequenceBinding.create(sequence3082, 2));
    sequenceBinding3085 = insert(ProgramSequenceBinding.create(sequence3082, 3));
    sequenceBinding3086 = insert(ProgramSequenceBinding.create(sequence3082, 4));
    sequenceBinding3087 = insert(ProgramSequenceBinding.create(sequence3082, 7));
    sequenceBinding3088 = insert(ProgramSequenceBinding.create(sequence3082, 6));
    sequenceBinding3089 = insert(ProgramSequenceBinding.create(sequence3082, 5));
    sequenceBinding3090 = insert(ProgramSequenceBinding.create(sequence3082, 0));
    sequenceBinding3091 = insert(ProgramSequenceBinding.create(sequence3082, 1));
    sequenceBinding3092 = insert(ProgramSequenceBinding.create(sequence3082, 8));
    // Program Sequence Binding Memes
    sequenceBindingMeme3093 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3087, "Steady"));
    sequenceBindingMeme3094 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3089, "Steady"));
    sequenceBindingMeme3095 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3084, "Steady"));
    sequenceBindingMeme3096 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3086, "Steady"));
    sequenceBindingMeme3097 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3090, "Steady"));
    sequenceBindingMeme3098 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3091, "Steady"));
    sequenceBindingMeme3099 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3085, "Steady"));
    sequenceBindingMeme3100 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3088, "Steady"));
    sequenceBindingMeme3101 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3092, "Break"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Main-type Program Windu Kush
    program78 = insert(Program.create(user27, library3, "Main", "Published", "Windu Kush", "Eb-", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program78, "Wind"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence3102 = insert(ProgramSequence.create(program78, 32, "A", 0.600000, "Eb-", 130.000000));
    sequence3103 = insert(ProgramSequence.create(program78, 8, "I", 0.600000, "Eb-", 130.000000));
    // Program Sequence Chords
    sequenceChord3104 = insert(ProgramSequenceChord.create(sequence3103, 0.000000, "Bmaj7"));
    sequenceChord3105 = insert(ProgramSequenceChord.create(sequence3102, 8.000000, "Gb/Bb"));
    sequenceChord3106 = insert(ProgramSequenceChord.create(sequence3102, 28.000000, "Ab-7"));
    sequenceChord3107 = insert(ProgramSequenceChord.create(sequence3102, 28.000000, "Gbsus4/Db"));
    sequenceChord3108 = insert(ProgramSequenceChord.create(sequence3102, 16.000000, "Gb/Ab"));
    sequenceChord3109 = insert(ProgramSequenceChord.create(sequence3102, 0.000000, "Eb-7"));
    sequenceChord3110 = insert(ProgramSequenceChord.create(sequence3103, 0.000000, "Ab-7"));
    // Program Sequence Bindings
    sequenceBinding3111 = insert(ProgramSequenceBinding.create(sequence3102, 4));
    sequenceBinding3112 = insert(ProgramSequenceBinding.create(sequence3103, 3));
    sequenceBinding3113 = insert(ProgramSequenceBinding.create(sequence3103, 12));
    sequenceBinding3114 = insert(ProgramSequenceBinding.create(sequence3102, 18));
    sequenceBinding3115 = insert(ProgramSequenceBinding.create(sequence3103, 15));
    sequenceBinding3116 = insert(ProgramSequenceBinding.create(sequence3102, 16));
    sequenceBinding3117 = insert(ProgramSequenceBinding.create(sequence3102, 7));
    sequenceBinding3118 = insert(ProgramSequenceBinding.create(sequence3102, 10));
    sequenceBinding3119 = insert(ProgramSequenceBinding.create(sequence3103, 0));
    sequenceBinding3120 = insert(ProgramSequenceBinding.create(sequence3103, 14));
    sequenceBinding3121 = insert(ProgramSequenceBinding.create(sequence3102, 5));
    sequenceBinding3122 = insert(ProgramSequenceBinding.create(sequence3103, 8));
    sequenceBinding3123 = insert(ProgramSequenceBinding.create(sequence3103, 2));
    sequenceBinding3124 = insert(ProgramSequenceBinding.create(sequence3102, 6));
    sequenceBinding3125 = insert(ProgramSequenceBinding.create(sequence3102, 11));
    sequenceBinding3126 = insert(ProgramSequenceBinding.create(sequence3103, 9));
    sequenceBinding3127 = insert(ProgramSequenceBinding.create(sequence3103, 13));
    sequenceBinding3128 = insert(ProgramSequenceBinding.create(sequence3102, 17));
    sequenceBinding3129 = insert(ProgramSequenceBinding.create(sequence3103, 1));
    // Program Sequence Binding Memes
    sequenceBindingMeme3130 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3111, "Steady"));
    sequenceBindingMeme3131 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3112, "Steady"));
    sequenceBindingMeme3132 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3119, "Steady"));
    sequenceBindingMeme3133 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3129, "Steady"));
    sequenceBindingMeme3134 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3127, "Steady"));
    sequenceBindingMeme3135 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3125, "Steady"));
    sequenceBindingMeme3136 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3121, "Steady"));
    sequenceBindingMeme3137 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3120, "Steady"));
    sequenceBindingMeme3138 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3113, "Steady"));
    sequenceBindingMeme3139 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3115, "Steady"));
    sequenceBindingMeme3140 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3124, "Steady"));
    sequenceBindingMeme3141 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3128, "Steady"));
    sequenceBindingMeme3142 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3114, "Break"));
    sequenceBindingMeme3143 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3123, "Steady"));
    sequenceBindingMeme3144 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3126, "Break"));
    sequenceBindingMeme3145 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3122, "Steady"));
    sequenceBindingMeme3146 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3118, "Steady"));
    sequenceBindingMeme3147 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3116, "Steady"));
    sequenceBindingMeme3148 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3117, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events
  }

  private void go16() throws Exception {
    // Insert Main-type Program Windy Baby Hey
    program90 = insert(Program.create(user27, library3, "Main", "Published", "Windy Baby Hey", "F", 130.000000, 0.600000));
    // Program Memes
    insert(ProgramMeme.create(program90, "Wind"));
    // Program Voices
    // Program Voice Tracks
    // Program Sequences
    sequence3149 = insert(ProgramSequence.create(program90, 64, "A", 0.600000, "F", 130.000000));
    // Program Sequence Chords
    sequenceChord3150 = insert(ProgramSequenceChord.create(sequence3149, 0.000000, "Csus4"));
    sequenceChord3151 = insert(ProgramSequenceChord.create(sequence3149, 32.000000, "Csus4/Bb"));
    sequenceChord3152 = insert(ProgramSequenceChord.create(sequence3149, 16.000000, "Csus4/D"));
    sequenceChord3153 = insert(ProgramSequenceChord.create(sequence3149, 48.000000, "Csus4/A"));
    // Program Sequence Bindings
    sequenceBinding3154 = insert(ProgramSequenceBinding.create(sequence3149, 0));
    sequenceBinding3155 = insert(ProgramSequenceBinding.create(sequence3149, 1));
    sequenceBinding3156 = insert(ProgramSequenceBinding.create(sequence3149, 7));
    sequenceBinding3157 = insert(ProgramSequenceBinding.create(sequence3149, 5));
    sequenceBinding3158 = insert(ProgramSequenceBinding.create(sequence3149, 3));
    sequenceBinding3159 = insert(ProgramSequenceBinding.create(sequence3149, 6));
    sequenceBinding3160 = insert(ProgramSequenceBinding.create(sequence3149, 2));
    sequenceBinding3161 = insert(ProgramSequenceBinding.create(sequence3149, 4));
    // Program Sequence Binding Memes
    sequenceBindingMeme3162 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3159, "Steady"));
    sequenceBindingMeme3163 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3154, "Steady"));
    sequenceBindingMeme3164 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3157, "Steady"));
    sequenceBindingMeme3165 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3158, "Break"));
    sequenceBindingMeme3166 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3161, "Steady"));
    sequenceBindingMeme3167 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3156, "Break"));
    sequenceBindingMeme3168 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3160, "Steady"));
    sequenceBindingMeme3169 = insert(ProgramSequenceBindingMeme.create(sequenceBinding3155, "Steady"));
    // Program Sequence Patterns
    // Program Sequence Pattern Events


    // Insert Rhythm-type Program 2-Step Prototype (Break)
    program75 = insert(Program.create(user3, library3, "Rhythm", "Published", "2-Step Prototype (Break)", "C", 130.000000, 0.000000));
    // Program Memes
    insert(ProgramMeme.create(program75, "Fire"));
    insert(ProgramMeme.create(program75, "Earth"));
    insert(ProgramMeme.create(program75, "Wind"));
    insert(ProgramMeme.create(program75, "Break"));
    insert(ProgramMeme.create(program75, "Water"));
    // Program Voices
    voice3170 = insert(ProgramVoice.create(program75, "Percussive", "Extra"));
    voice3171 = insert(ProgramVoice.create(program75, "Percussive", "Kick/Snare B"));
    voice3172 = insert(ProgramVoice.create(program75, "Percussive", "Kick/Snare A"));
    voice3173 = insert(ProgramVoice.create(program75, "Percussive", "Locomotion"));
    // Program Voice Tracks
    track3174 = insert(ProgramVoiceTrack.create(voice3171, "SNARE"));
    track3175 = insert(ProgramVoiceTrack.create(voice3173, "HIHATOPEN"));
    track3176 = insert(ProgramVoiceTrack.create(voice3170, "TOM"));
    track3177 = insert(ProgramVoiceTrack.create(voice3172, "KICK"));
    track3178 = insert(ProgramVoiceTrack.create(voice3173, "HIHAT"));
    track3179 = insert(ProgramVoiceTrack.create(voice3170, "CRASH"));
    // Program Sequences
    sequence3180 = insert(ProgramSequence.create(program75, 16, "Beat", 0.000000, "C", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding3181 = insert(ProgramSequenceBinding.create(sequence3180, 0));
    // Program Sequence Binding Memes
    // Program Sequence Patterns
    pattern3182 = insert(ProgramSequencePattern.create(sequence3180, voice3172, "Loop", 4, "Loop D"));
    pattern3183 = insert(ProgramSequencePattern.create(sequence3180, voice3172, "Intro", 4, "Intro A"));
    pattern3184 = insert(ProgramSequencePattern.create(sequence3180, voice3171, "Loop", 4, "Loop D"));
    pattern3185 = insert(ProgramSequencePattern.create(sequence3180, voice3173, "Loop", 4, "Loop B"));
    pattern3186 = insert(ProgramSequencePattern.create(sequence3180, voice3170, "Loop", 4, "Loop B"));
    pattern3187 = insert(ProgramSequencePattern.create(sequence3180, voice3172, "Loop", 4, "Loop A"));
    pattern3188 = insert(ProgramSequencePattern.create(sequence3180, voice3173, "Loop", 4, "Loop D"));
    pattern3189 = insert(ProgramSequencePattern.create(sequence3180, voice3173, "Intro", 4, "Intro A"));
    pattern3190 = insert(ProgramSequencePattern.create(sequence3180, voice3170, "Outro", 4, "Outro A"));
    pattern3191 = insert(ProgramSequencePattern.create(sequence3180, voice3171, "Outro", 4, "Outro A"));
    pattern3192 = insert(ProgramSequencePattern.create(sequence3180, voice3171, "Loop", 4, "Loop C"));
    pattern3193 = insert(ProgramSequencePattern.create(sequence3180, voice3171, "Loop", 4, "Loop A"));
    pattern3194 = insert(ProgramSequencePattern.create(sequence3180, voice3172, "Loop", 4, "Loop B"));
    pattern3195 = insert(ProgramSequencePattern.create(sequence3180, voice3171, "Loop", 4, "Loop B"));
    pattern3196 = insert(ProgramSequencePattern.create(sequence3180, voice3170, "Loop", 4, "Loop D"));
    pattern3197 = insert(ProgramSequencePattern.create(sequence3180, voice3170, "Loop", 4, "Loop C"));
    pattern3198 = insert(ProgramSequencePattern.create(sequence3180, voice3171, "Intro", 4, "Intro A"));
    pattern3199 = insert(ProgramSequencePattern.create(sequence3180, voice3170, "Intro", 4, "Intro A"));
    pattern3200 = insert(ProgramSequencePattern.create(sequence3180, voice3173, "Loop", 4, "Loop C"));
    pattern3201 = insert(ProgramSequencePattern.create(sequence3180, voice3170, "Loop", 4, "Loop A"));
    pattern3202 = insert(ProgramSequencePattern.create(sequence3180, voice3173, "Loop", 4, "Loop A"));
    pattern3203 = insert(ProgramSequencePattern.create(sequence3180, voice3172, "Loop", 4, "Loop C"));
    pattern3204 = insert(ProgramSequencePattern.create(sequence3180, voice3173, "Outro", 4, "Outro A"));
    pattern3205 = insert(ProgramSequencePattern.create(sequence3180, voice3172, "Outro", 4, "Outro A"));
    // Program Sequence Pattern Events
    event3206 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 1.250000, 0.250000, "G12", 0.100000));
    event3207 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 3.250000, 0.250000, "X", 0.330000));
    event3208 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3175, 0.500000, 0.500000, "X", 0.200000));
    event3209 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 0.250000, 0.250000, "G12", 0.100000));
    event3210 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 1.000000, 0.250000, "X", 0.050000));
    event3211 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 3.000000, 0.250000, "X", 0.050000));
    event3212 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 0.250000, 0.250000, "G12", 0.100000));
    event3213 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 2.000000, 0.250000, "X", 0.050000));
    event3214 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 0.250000, 0.250000, "G12", 0.100000));
    event3215 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3197, track3176, 2.500000, 1.500000, "X", 0.100000));
    event3216 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 0.750000, 0.250000, "X", 0.050000));
    event3217 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3191, track3174, 2.750000, 0.250000, "X", 0.100000));
    event3218 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3175, 3.500000, 0.500000, "X", 0.200000));
    event3219 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3175, 3.500000, 0.500000, "X", 0.200000));
    event3220 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 2.000000, 0.250000, "X", 0.050000));
    event3221 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 1.750000, 0.250000, "X", 0.100000));
    event3222 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 2.000000, 0.250000, "X", 0.050000));
    event3223 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3175, 2.500000, 0.500000, "X", 0.200000));
    event3224 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 2.250000, 0.250000, "E8", 0.100000));
    event3225 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 3.000000, 0.250000, "X", 0.050000));
    event3226 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3175, 3.500000, 0.500000, "X", 0.200000));
    event3227 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 3.750000, 0.250000, "X", 0.100000));
    event3228 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 0.000000, 0.250000, "X", 0.050000));
    event3229 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 2.750000, 0.250000, "X", 0.100000));
    event3230 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 1.750000, 0.250000, "X", 0.050000));
    event3231 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 2.750000, 0.250000, "X", 0.100000));
    event3232 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3205, track3177, 3.500000, 0.500000, "X", 0.500000));
    event3233 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 1.000000, 0.250000, "X", 0.050000));
    event3234 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3197, track3176, 1.750000, 0.750000, "X", 0.330000));
    event3235 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 1.750000, 0.250000, "X", 0.100000));
    event3236 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 3.750000, 0.250000, "X", 0.100000));
    event3237 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 0.000000, 0.250000, "X", 0.050000));
    event3238 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 0.750000, 0.250000, "X", 0.050000));
    event3239 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3175, 2.500000, 0.500000, "X", 0.200000));
    event3240 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3175, 2.500000, 0.500000, "X", 0.200000));
    event3241 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 3.250000, 0.250000, "G12", 0.100000));
    event3242 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3175, 0.500000, 0.500000, "X", 0.200000));
    event3243 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 3.250000, 0.250000, "X", 0.330000));
    event3244 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 1.250000, 0.250000, "G12", 0.100000));
    event3245 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 0.000000, 0.250000, "X", 0.050000));
    event3246 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3175, 0.500000, 0.500000, "X", 0.200000));
    event3247 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 2.250000, 0.250000, "E8", 0.100000));
    event3248 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 3.000000, 0.250000, "X", 0.050000));
    event3249 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3191, track3174, 3.000000, 1.000000, "X", 0.620000));
    event3250 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 1.000000, 0.250000, "X", 0.050000));
    event3251 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 3.250000, 0.250000, "G12", 0.100000));
    event3252 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 2.000000, 0.250000, "X", 0.050000));
    event3253 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3175, 0.500000, 0.500000, "X", 0.200000));
    event3254 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 1.250000, 0.250000, "G12", 0.100000));
    event3255 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 1.750000, 0.250000, "X", 0.100000));
    event3256 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 1.250000, 0.250000, "G12", 0.100000));
    event3257 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 3.750000, 0.250000, "X", 0.100000));
    event3258 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 2.000000, 0.250000, "X", 0.050000));
    event3259 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 1.000000, 0.250000, "E12", 0.050000));
    event3260 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 1.750000, 0.250000, "X", 0.100000));
    event3261 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 3.000000, 0.250000, "X", 0.050000));
    event3262 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 3.750000, 0.250000, "X", 0.100000));
    event3263 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3175, 1.500000, 0.500000, "X", 0.200000));
    event3264 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 3.250000, 0.250000, "G12", 0.100000));
    event3265 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3175, 3.500000, 0.500000, "X", 0.200000));
    event3266 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 3.750000, 0.250000, "X", 0.050000));
    event3267 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3175, 2.500000, 0.500000, "X", 0.200000));
    event3268 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 3.250000, 0.250000, "G12", 0.100000));
    event3269 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3175, 2.500000, 0.500000, "X", 0.200000));
    event3270 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 1.250000, 0.250000, "G12", 0.100000));
    event3271 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 2.750000, 0.250000, "X", 0.050000));
    event3272 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3196, track3179, 0.000000, 4.000000, "X", 0.620000));
    event3273 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 2.750000, 0.250000, "X", 0.100000));
    event3274 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 0.750000, 0.250000, "X", 0.050000));
    event3275 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 0.250000, 0.250000, "G12", 0.100000));
    event3276 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3175, 1.500000, 0.500000, "X", 0.200000));
    event3277 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3175, 1.500000, 0.500000, "X", 0.200000));
    event3278 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3175, 0.500000, 0.500000, "X", 0.200000));
    event3279 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 2.250000, 0.250000, "E8", 0.100000));
    event3280 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3175, 1.500000, 0.500000, "X", 0.200000));
    event3281 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 2.250000, 0.250000, "E8", 0.100000));
    event3282 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 0.750000, 0.250000, "X", 0.050000));
    event3283 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3175, 1.500000, 0.500000, "X", 0.200000));
    event3284 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3175, 3.500000, 0.500000, "X", 0.200000));
    event3285 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 0.000000, 0.250000, "X", 0.050000));
    event3286 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 2.250000, 0.250000, "E8", 0.100000));
    event3287 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 0.000000, 0.250000, "X", 0.050000));
    event3288 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 1.000000, 0.250000, "X", 0.050000));
    event3289 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 0.750000, 0.250000, "X", 0.050000));
    event3290 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 3.000000, 0.250000, "X", 0.050000));
    event3291 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3175, 2.500000, 0.500000, "X", 0.200000));
    event3292 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 1.000000, 0.250000, "X", 0.050000));
    event3293 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3175, 3.500000, 0.500000, "X", 0.200000));
    event3294 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3178, 2.000000, 0.250000, "X", 0.050000));
    event3295 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3186, track3176, 1.500000, 2.500000, "X", 0.100000));
    event3296 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 3.000000, 0.250000, "X", 0.050000));
    event3297 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3202, track3178, 2.250000, 0.250000, "E8", 0.100000));
    event3298 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3199, track3179, 0.000000, 4.000000, "X", 0.620000));
    event3299 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 1.250000, 0.250000, "G12", 0.100000));
    event3300 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 2.750000, 0.250000, "X", 0.100000));
    event3301 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3200, track3178, 0.750000, 0.250000, "X", 0.050000));
    event3302 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 0.000000, 0.250000, "X", 0.050000));
    event3303 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 0.250000, 0.250000, "G12", 0.100000));
    event3304 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 0.250000, 0.250000, "X", 0.100000));
    event3305 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3178, 3.750000, 0.250000, "X", 0.100000));
    event3306 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3189, track3178, 1.750000, 0.250000, "X", 0.100000));
    event3307 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3186, track3176, 0.750000, 0.750000, "X", 0.330000));
    event3308 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3188, track3175, 0.500000, 0.500000, "X", 0.200000));
    event3309 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3204, track3175, 1.500000, 0.500000, "X", 0.200000));
    event3310 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3185, track3178, 2.750000, 0.250000, "X", 0.100000));

  }

  private void go17() throws Exception {
    // Insert Rhythm-type Program 2-Step Prototype (Steady)
    program29 = insert(Program.create(user3, library3, "Rhythm", "Published", "2-Step Prototype (Steady)", "C", 130.000000, 0.000000));
    // Program Memes
    insert(ProgramMeme.create(program29, "Fire"));
    insert(ProgramMeme.create(program29, "Steady"));
    insert(ProgramMeme.create(program29, "Wind"));
    insert(ProgramMeme.create(program29, "Water"));
    insert(ProgramMeme.create(program29, "Earth"));
    // Program Voices
    voice3311 = insert(ProgramVoice.create(program29, "Percussive", "Kick/Snare A"));
    voice3312 = insert(ProgramVoice.create(program29, "Percussive", "Kick/Snare B"));
    voice3313 = insert(ProgramVoice.create(program29, "Percussive", "Extra"));
    voice3314 = insert(ProgramVoice.create(program29, "Percussive", "Locomotion"));
    // Program Voice Tracks
    track3315 = insert(ProgramVoiceTrack.create(voice3312, "SNARE"));
    track3316 = insert(ProgramVoiceTrack.create(voice3313, "CRASH"));
    track3317 = insert(ProgramVoiceTrack.create(voice3314, "HIHAT"));
    track3318 = insert(ProgramVoiceTrack.create(voice3313, "TOM"));
    track3319 = insert(ProgramVoiceTrack.create(voice3314, "HIHATOPEN"));
    track3320 = insert(ProgramVoiceTrack.create(voice3311, "KICK"));
    // Program Sequences
    sequence3321 = insert(ProgramSequence.create(program29, 16, "Beat", 0.000000, "C", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding3322 = insert(ProgramSequenceBinding.create(sequence3321, 0));
    // Program Sequence Binding Memes
    // Program Sequence Patterns
    pattern3323 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Outro", 4, "Outro D"));
    pattern3324 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Outro", 4, "Outro B"));
    pattern3325 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Outro", 4, "Outro C"));
    pattern3326 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Loop", 4, "Loop A"));
    pattern3327 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Outro", 4, "Outro D"));
    pattern3328 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Outro", 4, "Outro A"));
    pattern3329 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Outro", 4, "Outro A"));
    pattern3330 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Outro", 4, "Outro C"));
    pattern3331 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Outro", 4, "Outro A"));
    pattern3332 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Loop", 4, "Loop A"));
    pattern3333 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Outro", 4, "Outro D"));
    pattern3334 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Loop", 4, "Loop A (+kick on 3-and)"));
    pattern3335 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Outro", 4, "Outro F"));
    pattern3336 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Intro", 4, "Intro A"));
    pattern3337 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Loop", 4, "Loop A (tom on 4-and)"));
    pattern3338 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Loop", 4, "Loop A (+kick on 3-and)"));
    pattern3339 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Loop", 4, "Loop A (tom on 1-and)"));
    pattern3340 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Outro", 4, "Outro F"));
    pattern3341 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Loop", 4, "Loop A (tom on 3-and)"));
    pattern3342 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Outro", 4, "Outro F"));
    pattern3343 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Loop", 4, "Loop A (tom on 1-and)"));
    pattern3344 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Outro", 4, "Outro E"));
    pattern3345 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Loop", 4, "Loop A (tom on 2-and)"));
    pattern3346 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Outro", 4, "Outro C"));
    pattern3347 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Loop", 4, "Loop A"));
    pattern3348 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Outro", 4, "Outro A"));
    pattern3349 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Loop", 4, "Loop A (tom on 1-and)"));
    pattern3350 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Loop", 4, "Loop A (+snare on 3-and)"));
    pattern3351 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Outro", 4, "Outro E"));
    pattern3352 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Outro", 4, "Outro B"));
    pattern3353 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Outro", 4, "Outro C"));
    pattern3354 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Loop", 4, "Loop A (+snare on 3-and)"));
    pattern3355 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Loop", 4, "Loop A (tom on 3-and)"));
    pattern3356 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Loop", 4, "Loop A (tom on 4-and)"));
    pattern3357 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Loop", 4, "Loop A"));
    pattern3358 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Loop", 4, "Loop A (tom on 4-and)"));
    pattern3359 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Outro", 4, "Outro B"));
    pattern3360 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Outro", 4, "Outro D"));
    pattern3361 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Loop", 4, "Loop A (tom on 1-and)"));
    pattern3362 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Outro", 4, "Outro E"));
    pattern3363 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Loop", 4, "Loop A (tom on 3-and)"));
    pattern3364 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Intro", 4, "Intro A"));
    pattern3365 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Outro", 4, "Outro B"));
    pattern3366 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Loop", 4, "Loop A (tom on 2-and)"));
    pattern3367 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Loop", 4, "Loop A (tom on 2-and)"));
    pattern3368 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Loop", 4, "Loop A (+kick on 3-and)"));
    pattern3369 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Outro", 4, "Outro F"));
    pattern3370 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Loop", 4, "Loop A (tom on 3-and)"));
    pattern3371 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Loop", 4, "Loop A (+snare on 3-and)"));
    pattern3372 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Loop", 4, "Loop A (tom on 2-and)"));
    pattern3373 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Loop", 4, "Loop A (+snare on 3-and)"));
    pattern3374 = insert(ProgramSequencePattern.create(sequence3321, voice3314, "Outro", 4, "Outro E"));
    pattern3375 = insert(ProgramSequencePattern.create(sequence3321, voice3313, "Intro", 4, "Intro A"));
    pattern3376 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Loop", 4, "Loop A (+kick on 3-and)"));
    pattern3377 = insert(ProgramSequencePattern.create(sequence3321, voice3312, "Intro", 4, "Intro A"));
    pattern3378 = insert(ProgramSequencePattern.create(sequence3321, voice3311, "Loop", 4, "Loop A (tom on 4-and)"));
    // Program Sequence Pattern Events
    event3379 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3380 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3317, 3.250000, 0.250000, "G12", 0.100000));
    event3381 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3357, track3320, 2.250000, 1.750000, "X", 0.050000));
    event3382 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3317, 2.750000, 0.500000, "X", 0.100000));
    event3383 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3384 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3317, 1.750000, 0.500000, "X", 0.050000));
    event3385 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3332, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3386 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3346, track3318, 3.500000, 0.500000, "X", 0.050000));
    event3387 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3317, 3.250000, 0.250000, "X", 0.330000));
    event3388 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3317, 3.250000, 0.250000, "G12", 0.100000));
    event3389 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3390 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3391 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3317, 0.750000, 0.500000, "X", 0.050000));
    event3392 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3393 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3394 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3317, 0.750000, 0.500000, "X", 0.050000));
    event3395 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3376, track3320, 1.750000, 0.500000, "X", 0.010000));
    event3396 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3397 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3398 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3336, track3320, 1.750000, 2.250000, "X", 0.050000));
    event3399 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3400 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3317, 3.750000, 0.250000, "X", 0.100000));
    event3401 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3360, track3320, 3.500000, 0.500000, "X", 0.330000));
    event3402 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3351, track3315, 1.000000, 1.750000, "X", 0.620000));
    event3403 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3404 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3405 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3361, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3406 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3366, track3320, 2.000000, 1.250000, "X", 0.010000));
    event3407 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3408 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3357, track3320, 0.000000, 1.750000, "X", 0.620000));
    event3409 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3342, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3410 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3411 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3412 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3413 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3360, track3320, 0.250000, 1.500000, "X", 0.050000));
    event3414 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3415 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3335, track3316, 3.000000, 1.000000, "X", 0.204600));
    event3416 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3361, track3315, 1.000000, 2.000000, "X", 0.620000));
    event3417 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3354, track3320, 0.500000, 3.500000, "X", 0.500000));
    event3418 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3334, track3315, 1.000000, 2.000000, "X", 0.620000));
    event3419 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3317, 2.750000, 0.500000, "X", 0.050000));
    event3420 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3327, track3315, 1.000000, 1.750000, "X", 0.620000));
    event3421 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3317, 3.250000, 0.250000, "X", 0.330000));
    event3422 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3368, track3320, 3.250000, 0.750000, "X", 0.050000));
    event3423 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3317, 2.750000, 0.500000, "X", 0.050000));
    event3424 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3357, track3320, 1.750000, 0.500000, "X", 0.010000));
    event3425 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3317, 1.750000, 0.500000, "X", 0.050000));
    event3426 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3427 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3428 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3317, 0.750000, 0.500000, "X", 0.050000));
    event3429 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3317, 2.750000, 0.500000, "X", 0.100000));
    event3430 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3431 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3317, 2.750000, 0.500000, "X", 0.050000));
    event3432 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3362, track3318, 3.750000, 0.250000, "X", 0.100000));
    event3433 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3434 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3317, 1.750000, 0.500000, "X", 0.100000));
    event3435 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3436 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3437 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3438 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3362, track3318, 3.500000, 0.250000, "X", 0.100000));
    event3439 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3317, 1.000000, 0.250000, "E12", 0.000000));
    event3440 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3441 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3442 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3443 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3341, track3320, 1.750000, 2.250000, "X", 0.050000));
    event3444 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3445 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3363, track3318, 2.500000, 1.500000, "X", 0.330000));
    event3446 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3317, 0.750000, 0.250000, "X", 0.050000));
    event3447 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3317, 1.000000, 0.250000, "E12", 0.000000));
    event3448 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3449 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3450 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3451 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3452 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3378, track3320, 0.000000, 1.750000, "X", 0.620000));
    event3453 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3376, track3320, 0.500000, 1.250000, "X", 0.500000));
    event3454 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3317, 1.000000, 0.250000, "E12", 0.000000));
    event3455 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3456 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3317, 1.750000, 0.500000, "X", 0.050000));
    event3457 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3458 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3317, 0.250000, 0.250000, "X", 0.100000));
    event3459 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3460 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3323, track3318, 1.500000, 2.500000, "X", 0.330000));
    event3461 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3462 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3365, track3318, 2.500000, 1.500000, "X", 0.050000));
    event3463 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3464 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3377, track3315, 1.000000, 2.000000, "X", 0.620000));
    event3465 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3375, track3316, 0.000000, 4.000000, "X", 0.620000));
    event3466 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3467 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3317, 0.750000, 0.500000, "X", 0.050000));
    event3468 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3469 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3317, 3.750000, 0.250000, "X", 0.100000));
    event3470 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3377, track3315, 3.000000, 1.000000, "X", 1.000000));
    event3471 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3339, track3320, 1.750000, 2.250000, "X", 0.010000));
    event3472 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3317, 2.750000, 0.500000, "X", 0.100000));
    event3473 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3317, 2.750000, 0.500000, "X", 0.100000));
    event3474 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3331, track3315, 1.000000, 1.750000, "X", 0.620000));
    event3475 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3476 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3477 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3478 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3479 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3372, track3318, 1.500000, 2.500000, "X", 0.330000));
    event3480 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3317, 0.750000, 0.500000, "X", 0.050000));
    event3481 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3317, 1.750000, 0.500000, "X", 0.100000));
    event3482 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3483 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3344, track3320, 3.500000, 0.500000, "X", 0.330000));
    event3484 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3485 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3367, track3315, 1.000000, 2.000000, "X", 0.620000));
    event3486 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3487 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3330, track3320, 0.500000, 1.250000, "X", 0.500000));
    event3488 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3489 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3373, track3315, 2.500000, 0.500000, "X", 0.100000));
    event3490 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3317, 1.000000, 0.250000, "E12", 0.000000));
    event3491 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3492 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3317, 0.750000, 0.250000, "X", 0.050000));
    event3493 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3494 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3495 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3327, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3496 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3359, track3315, 1.000000, 2.000000, "X", 0.620000));
    event3497 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3498 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3317, 1.000000, 0.250000, "E12", 0.000000));
    event3499 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3317, 2.750000, 0.500000, "X", 0.050000));
    event3500 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3317, 0.750000, 0.250000, "X", 0.050000));
    event3501 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3502 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3503 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3365, track3318, 1.750000, 0.750000, "X", 0.100000));
    event3504 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3505 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3317, 1.750000, 0.500000, "X", 0.050000));
    event3506 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3507 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3317, 3.250000, 0.250000, "X", 0.330000));
    event3508 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3317, 0.750000, 0.250000, "X", 0.050000));
    event3509 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3317, 3.750000, 0.250000, "X", 0.050000));
    event3510 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3511 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3512 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3513 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3514 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3317, 1.750000, 0.500000, "X", 0.100000));
    event3515 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3367, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3516 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3317, 3.250000, 0.250000, "X", 0.330000));
    event3517 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3317, 3.250000, 0.250000, "G12", 0.100000));
    event3518 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3317, 3.250000, 0.250000, "G12", 0.100000));
    event3519 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3356, track3315, 1.000000, 2.000000, "X", 0.620000));
    event3520 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3317, 0.750000, 0.250000, "X", 0.050000));
    event3521 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3317, 3.250000, 0.250000, "X", 0.330000));
    event3522 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3523 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3524 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3525 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3346, track3318, 2.750000, 0.750000, "X", 0.100000));
    event3526 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3527 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3330, track3320, 2.250000, 1.750000, "X", 0.050000));
    event3528 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3351, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3529 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3369, track3320, 0.500000, 2.000000, "X", 0.500000));
    event3530 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3317, 1.750000, 0.500000, "X", 0.100000));
    event3531 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3355, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3532 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3359, track3320, 0.500000, 0.500000, "X", 0.500000));
    event3533 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3373, track3315, 1.000000, 1.500000, "X", 0.620000));
    event3534 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3369, track3320, 3.500000, 0.500000, "X", 0.100000));
    event3535 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3317, 0.750000, 0.500000, "X", 0.050000));
    event3536 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3537 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3538 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3539 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3317, 3.750000, 0.250000, "X", 0.100000));
    event3540 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3317, 1.750000, 0.500000, "X", 0.050000));
    event3541 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3353, track3315, 1.000000, 2.000000, "X", 0.620000));
    event3542 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3543 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3366, track3320, 3.250000, 0.750000, "X", 0.050000));
    event3544 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3317, 2.750000, 0.500000, "X", 0.050000));
    event3545 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3317, 1.000000, 0.250000, "E12", 0.000000));
    event3546 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3317, 0.750000, 0.500000, "X", 0.050000));
    event3547 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3548 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3344, track3320, 0.000000, 2.000000, "X", 0.620000));
    event3549 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3317, 3.750000, 0.250000, "X", 0.050000));
    event3550 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3373, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3551 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3334, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3552 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3317, 1.750000, 0.500000, "X", 0.100000));
    event3553 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3331, track3315, 2.750000, 0.250000, "X", 0.100000));
    event3554 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3328, track3320, 0.500000, 1.500000, "X", 0.500000));
    event3555 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3359, track3315, 3.000000, 0.500000, "X", 0.620000));
    event3556 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3557 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3330, track3320, 1.750000, 0.500000, "X", 0.010000));
    event3558 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3327, track3315, 2.750000, 0.250000, "X", 0.100000));
    event3559 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3560 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3561 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3317, 2.750000, 0.500000, "X", 0.100000));
    event3562 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3359, track3320, 3.500000, 0.500000, "X", 0.200000));
    event3563 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3564 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3565 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3328, track3320, 2.000000, 1.500000, "X", 0.050000));
    event3566 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3317, 1.750000, 0.500000, "X", 0.100000));
    event3567 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3353, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3568 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3317, 1.750000, 0.500000, "X", 0.100000));
    event3569 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3356, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3570 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3571 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3337, track3317, 3.750000, 0.250000, "X", 0.100000));
    event3572 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3573 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3574 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3317, 3.250000, 0.250000, "G12", 0.100000));
    event3575 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3576 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3577 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3578 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3317, 1.750000, 0.500000, "X", 0.100000));
    event3579 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3331, track3315, 3.000000, 1.000000, "X", 0.620000));
    event3580 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3317, 3.750000, 0.250000, "X", 0.100000));
    event3581 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3342, track3315, 1.000000, 2.000000, "X", 0.620000));
    event3582 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3317, 2.750000, 0.500000, "X", 0.100000));
    event3583 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3360, track3320, 1.750000, 1.750000, "X", 0.050000));
    event3584 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3362, track3318, 3.000000, 0.250000, "X", 0.100000));
    event3585 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3317, 1.750000, 0.500000, "X", 0.050000));
    event3586 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3317, 3.250000, 0.250000, "G12", 0.100000));
    event3587 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3362, track3318, 3.250000, 0.250000, "X", 0.100000));
    event3588 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3589 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3590 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3364, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3591 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3592 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3593 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3376, track3320, 2.250000, 1.750000, "X", 0.000000));
    event3594 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3317, 0.750000, 0.500000, "X", 0.050000));
    event3595 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3317, 0.250000, 0.250000, "G12", 0.100000));
    event3596 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3317, 3.750000, 0.250000, "X", 0.050000));
    event3597 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3317, 2.750000, 0.500000, "X", 0.050000));
    event3598 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3343, track3318, 0.500000, 3.500000, "X", 0.330000));
    event3599 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3600 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3371, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3601 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3317, 3.750000, 0.250000, "X", 0.100000));
    event3602 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3325, track3317, 3.750000, 0.250000, "X", 0.050000));
    event3603 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3317, 0.750000, 0.250000, "X", 0.050000));
    event3604 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3317, 3.750000, 0.250000, "X", 0.050000));
    event3605 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3369, track3320, 2.500000, 1.000000, "X", 0.050000));
    event3606 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3336, track3320, 0.000000, 1.750000, "X", 1.000000));
    event3607 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3323, track3318, 0.750000, 0.750000, "X", 0.660000));
    event3608 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3319, 3.500000, 0.250000, "X", 0.200000));
    event3609 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3317, 2.750000, 0.500000, "X", 0.100000));
    event3610 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3317, 3.750000, 0.250000, "X", 0.100000));
    event3611 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3341, track3320, 0.000000, 1.750000, "X", 0.620000));
    event3612 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3613 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3349, track3317, 2.750000, 0.500000, "X", 0.100000));
    event3614 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3338, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3615 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3378, track3320, 1.750000, 2.250000, "X", 0.010000));
    event3616 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3617 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3326, track3317, 3.250000, 0.250000, "G12", 0.100000));
    event3618 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3358, track3318, 3.500000, 0.500000, "X", 0.330000));
    event3619 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3317, 2.250000, 0.250000, "E8", 0.100000));
    event3620 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3366, track3320, 0.000000, 2.000000, "X", 0.620000));
    event3621 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3317, 1.250000, 0.250000, "G12", 0.100000));
    event3622 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3317, 3.750000, 0.250000, "X", 0.100000));
    event3623 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3332, track3315, 1.000000, 2.000000, "X", 0.620000));
    event3624 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3351, track3315, 2.750000, 0.250000, "X", 0.100000));
    event3625 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3345, track3319, 0.500000, 0.250000, "X", 0.200000));
    event3626 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3374, track3317, 3.250000, 0.250000, "X", 0.330000));
    event3627 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3344, track3320, 2.000000, 1.500000, "X", 0.620000));
    event3628 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3629 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3329, track3317, 3.250000, 0.250000, "X", 0.330000));
    event3630 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3328, track3320, 3.500000, 0.500000, "X", 0.100000));
    event3631 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3333, track3317, 3.750000, 0.250000, "X", 0.050000));
    event3632 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3340, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3633 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3324, track3319, 2.500000, 0.250000, "X", 0.200000));
    event3634 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3370, track3319, 1.500000, 0.250000, "X", 0.200000));
    event3635 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3355, track3315, 1.000000, 2.000000, "X", 0.620000));
    event3636 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3339, track3320, 0.000000, 1.750000, "X", 0.620000));


    // Insert Rhythm-type Program 2-Step Shuffle Beat
    program6 = insert(Program.create(user1, library1, "Rhythm", "Published", "2-Step Shuffle Beat", "C", 130.000000, 0.000000));
    // Program Memes
    insert(ProgramMeme.create(program6, "Electro"));
    insert(ProgramMeme.create(program6, "Tropical"));
    insert(ProgramMeme.create(program6, "Progressive"));
    insert(ProgramMeme.create(program6, "Cool"));
    insert(ProgramMeme.create(program6, "Easy"));
    insert(ProgramMeme.create(program6, "Hot"));
    insert(ProgramMeme.create(program6, "Tech"));
    insert(ProgramMeme.create(program6, "Acid"));
    insert(ProgramMeme.create(program6, "Classic"));
    insert(ProgramMeme.create(program6, "Hard"));
    insert(ProgramMeme.create(program6, "Deep"));
    // Program Voices
    voice3637 = insert(ProgramVoice.create(program6, "Percussive", "Toms+Congas+Misc"));
    voice3638 = insert(ProgramVoice.create(program6, "Percussive", "Kick+Snare"));
    voice3639 = insert(ProgramVoice.create(program6, "Percussive", "2x4 Stomp"));
    voice3640 = insert(ProgramVoice.create(program6, "Percussive", "Locomotion"));
    voice3641 = insert(ProgramVoice.create(program6, "Percussive", "Clave"));
    voice3642 = insert(ProgramVoice.create(program6, "Percussive", "Vocal Echo"));
    voice3643 = insert(ProgramVoice.create(program6, "Percussive", "Vocal"));
    // Program Voice Tracks
    track3644 = insert(ProgramVoiceTrack.create(voice3641, "TOM"));
    track3645 = insert(ProgramVoiceTrack.create(voice3638, "SNARE"));
    track3646 = insert(ProgramVoiceTrack.create(voice3638, "KICK"));
    track3647 = insert(ProgramVoiceTrack.create(voice3640, "HIHAT"));
    track3648 = insert(ProgramVoiceTrack.create(voice3640, "HIHATOPEN"));
    track3649 = insert(ProgramVoiceTrack.create(voice3637, "CRASH"));
    // Program Sequences
    sequence3650 = insert(ProgramSequence.create(program6, 16, "Beat", 0.000000, "C", 130.000000));
    // Program Sequence Chords
    // Program Sequence Bindings
    sequenceBinding3651 = insert(ProgramSequenceBinding.create(sequence3650, 0));
    // Program Sequence Binding Memes
    // Program Sequence Patterns
    pattern3652 = insert(ProgramSequencePattern.create(sequence3650, voice3638, "Loop", 4, "drop d beet"));
    pattern3653 = insert(ProgramSequencePattern.create(sequence3650, voice3640, "Loop", 4, "drop d beet"));
    pattern3654 = insert(ProgramSequencePattern.create(sequence3650, voice3643, "Loop", 4, "drop d beet"));
    pattern3655 = insert(ProgramSequencePattern.create(sequence3650, voice3637, "Loop", 4, "drop d beet"));
    pattern3656 = insert(ProgramSequencePattern.create(sequence3650, voice3642, "Loop", 4, "drop d beet"));
    pattern3657 = insert(ProgramSequencePattern.create(sequence3650, voice3641, "Loop", 4, "drop d beet"));
    pattern3658 = insert(ProgramSequencePattern.create(sequence3650, voice3639, "Loop", 4, "drop d beet"));
    // Program Sequence Pattern Events
    event3659 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 0.250000, 0.200000, "G12", 0.100000));
    event3660 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 2.000000, 0.250000, "E12", 0.200000));
    event3661 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3644, 1.500000, 0.500000, "Bb8", 0.200000));
    event3662 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3644, 2.500000, 0.500000, "Bb8", 0.200000));
    event3663 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 1.750000, 0.200000, "D12", 0.120000));
    event3664 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3657, track3644, 1.000000, 0.500000, "G4", 0.050000));
    event3665 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3658, track3646, 0.000000, 1.000000, "C2", 0.620000));
    event3666 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3655, track3644, 3.500000, 0.500000, "G3", 0.100000));
    event3667 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3657, track3644, 3.500000, 1.000000, "G4", 0.100000));
    event3668 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3648, 1.500000, 0.250000, "E8", 0.200000));
    event3669 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3644, 0.500000, 0.500000, "Bb8", 0.200000));
    event3670 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3648, 2.500000, 0.250000, "G12", 0.200000));
    event3671 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 1.000000, 0.250000, "E12", 0.160000));
    event3672 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3652, track3646, 2.500000, 0.500000, "C2", 0.300000));
    event3673 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3652, track3646, 2.250000, 0.200000, "F#2", 0.200000));
    event3674 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3655, track3649, 2.000000, 1.000000, "F5", 0.031000));
    event3675 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 3.250000, 0.200000, "G12", 0.100000));
    event3676 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3652, track3645, 1.000000, 1.000000, "G8", 0.620000));
    event3677 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3657, track3644, 2.000000, 0.800000, "G4", 0.050000));
    event3678 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 3.000000, 0.250000, "E12", 0.200000));
    event3679 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3652, track3645, 3.000000, 1.000000, "G8", 0.620000));
    event3680 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 3.750000, 0.200000, "D12", 0.120000));
    event3681 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3658, track3646, 2.500000, 1.000000, "C2", 0.500000));
    event3682 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 2.250000, 0.200000, "E8", 0.100000));
    event3683 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3644, 3.250000, 0.500000, "Bb8", 0.100000));
    event3684 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 0.000000, 0.300000, "E12", 0.200000));
    event3685 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3655, track3644, 1.250000, 0.700000, "G5", 0.050000));
    event3686 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3648, 0.500000, 0.250000, "E8", 0.200000));
    event3687 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 0.750000, 0.200000, "D12", 0.025000));
    event3688 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3652, track3645, 1.750000, 0.200000, "G5", 0.100000));
    event3689 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3644, 3.500000, 0.500000, "Bb8", 0.200000));
    event3690 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3655, track3644, 0.500000, 0.750000, "C6", 0.100000));
    event3691 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3657, track3644, 1.000000, 1.000000, "G4", 0.100000));
    event3692 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3655, track3644, 2.000000, 1.000000, "C5", 0.200000));
    event3693 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3657, track3644, 2.750000, 0.800000, "G4", 0.080000));
    event3694 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 2.750000, 0.200000, "D12", 0.025000));
    event3695 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3647, 1.250000, 0.200000, "G12", 0.100000));
    event3696 = insert((ProgramSequencePatternEvent) ProgramSequencePatternEvent.create(pattern3653, track3648, 3.500000, 0.500000, "E8", 0.200000));

  }

  public void go() throws Exception {
    // Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
    go1();
    go2();
    go3();
    go4();
    go5();
    go6();
    go7();
    go8();
    go9();
    go10();
    go11();
    go12();
    go14();
    go15();
    go16();
    go17();

  }
}
