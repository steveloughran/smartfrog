%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%STATE SCOPING

:- dynamic clause_level/1.
:- dynamic clause_state/2.

level(L) :- (clause_level(L) -> 
                             true
                             ;
                             L=0).

sos :- (clause_level(L) -> 
                             retract(clause_level(L)), 
                             L1 is L+1,
                             assert(clause_level(L1))
                             ;
                             assert(clause_level(1))).

eos :- (clause_level(L) -> 
                             retract(clause_level(L)), 
                             L1 is L-1,
                             assert(clause_level(L1)),
                             retract_state_level(L)
                             ;
                             true).  %being kind

assert_state(C) :- level(L), assert(clause_state(L, C)).
asserta_state(C) :- level(L), asserta(clause_state(L, C)).
assertz_state(C) :- level(L), assertz(clause_state(L, C)).

replace_state_level(C) :-
        C=..[F|Args], 
        length(Args,N), length(ArgsR,N),
        CR=..[F|ArgsR],
        retract_state_level(CR),
        assert_state(C).

retract_all_state :- retract_all(clause_state(_,_)).
retract_all_state(C) :- retract_all(clause_state(_,C)).
retract_all_state_level(C) :- level(L), retract_all(clause_state(L,C)).
retract_all_state_level :- eos.

retract_state_level(L) :- retract_all(clause_state(L,_)).

retract_state(C) :- level(L), try_retract(L, C).
try_retract(L, C) :- retract(clause_state(L,C)),!.
try_retract(0, _) :- writeln("Failed to retract given"
                                         " clause state at any scope"
                             " level"); flush(stdout).
try_retract(L, C) :- L>0, L1 is L-1, try_retract(L1, C).

clause_state(C) :- level(L), try_clause(L, C).
try_clause(L, C) :- clause_state(L, C).
try_clause(L, C) :- L>0, L1 is L-1, try_clause(L1, C).

clause_state_level(C) :- level(L), clause_state(L, C).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%THEORY NAMESPACES

:- dynamic namespace/1.
:- dynamic root/1.

add_path(PT) :- assert(root(PT)). 

source_compose(FNIn,FNOut) :- 
        (root(PT) -> concat_strings(PT, FNIn, FNOut); FNOut=FNIn). 

source(FN) :- source_compose(FN,FNO), compile(FNO).
source(FN, NSS) :- (namespace(NSS) -> 
                       writeln("Namespace atom provided"
                            " already in use"), flush(stdout);
                    source_compose(FN,FNS),
                    source_compose(NSS, FNT1),
                    concat_strings(FNT1, FN, FNT),
                    do_compile(FNS, FNT, NSS),
                    assert(namespace(NSS))).


do_compile(FNS, FNT, NSS) :- open(FNS, read, S),
                     get_heads(S, [], [], Hs, Cls),
                     close(S),
                     concat_strings(NSS, "__", NSS1),
                     process_clauses(NSS1, Hs, Cls, [], Cls1),
                     open(FNT, write, OS),
                     writeclauses(OS, Cls1),
                     close(OS),
                     compile(FNT).

writeclauses(_,[]).
writeclauses(S,[H|T]) :-
        writeclause(S, H), 
        writeclauses(S, T).

get_heads(S, HsI, ClsI, HsO, ClsO):- 
              read(S, C),
              (C = end_of_file -> HsI=HsO, ClsI=ClsO;
              (C = (:- _ ) -> get_heads(S, HsI, [C|ClsI],
                                         HsO, ClsO);
              (C = (H :- _) ->                   
                               get_arity(H, F, A);
                               get_arity(C, F, A)),
                                                
                               (member((F,A),HsI) -> HsI1 = HsI;
                                                     HsI1 = [(F,A)|HsI]),
                               get_heads(S, HsI1, [C|ClsI], HsO, ClsO))).

get_arity(H, F, Ar) :- 
        H =.. [F|Args],
        length(Args, Ar).

process_clauses(_, _, [], Cls, Cls).
process_clauses(NSs, Hs, [HC|TC], ClsI, ClsO) :-
        
        (HC = (:- _ ) -> ClsI1 = [HC|ClsI];  
                                        
        (HC = (H :- T) -> process_head(NSs, H, NSH),
                          convert_tuple_list(T, TL),
                          process_tail(NSs, Hs, TL, NST), 
                          convert_tuple_list(NSTT, NST),
                          NSC = (NSH :- NSTT),
                          ClsI1 = [NSC | ClsI];
                          
                          process_head(NSs, HC, NSH), 
                          ClsI1 = [NSH | ClsI])),
                          
        process_clauses(NSs, Hs, TC, ClsI1, ClsO).

process_head(NSs, H, NSH):-
        H =..[F|Args],
        atom_string(F, Fs),
        concat_strings(NSs, Fs, F1s),
        atom_string(F1, F1s),
        NSH =..[F1|Args].

process_func(_, _, assert, [_], assert_state) :- !.
process_func(_, _, asserta, [_], asserta_state) :- !.
process_func(_, _, assertz, [_], assertz_state) :- !.

process_func(_, _, ecl_assert, [_], assert) :- !.
process_func(_, _, ecl_asserta, [_], asserta) :- !.
process_func(_, _, ecl_assertz, [_], assertz) :- !.

process_func(_, _, retract, [_], retract_state) :- !.
process_func(_, _, retract_all, [_], retract_all_state) :- !.

process_func(_, _, ecl_retract, [_], retract) :- !.
process_func(_, _, ecl_retract_all, [_], retract_all) :- !.

process_func(_, _, clause, [_], clause_state) :- !.

process_func(_, _, ecl_clause, [_], clause) :- !.

        
process_func(NSs, Hs, F, Args, F1):-
        length(Args, Ar),
        member((F, Ar), Hs), !,
        atom_string(F, Fs),
        concat_strings(NSs, Fs, F1s),
        atom_string(F1, F1s).
process_func(_, _, F, _, F).

process_tail(_, _, [], []).
process_tail(NSs, Hs, [H|T], [NSH|NST1]):-
        process_tail(NSs, Hs, T, NST1),
        process_atom(NSs, Hs, H, NSH).

process_atom(NSs, Hs, H, NSH) :-
        (nonvar(H) -> H =..[F|Args], 
                    process_func(NSs, Hs, F, Args, NSF), 
                    process_tail(NSs, Hs, Args, NSA),
                    NSH =..[NSF|NSA];
                    NSH=H).

convert_tuple_list((M,Ms), [M,Ms1|MsR]) :- !, convert_tuple_list(Ms, [Ms1|MsR]).
convert_tuple_list((M), [M]).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Preprocessing
:- lib(hash).
:- dynamic sfvcnt/1.

sfvcnt(1).

preprocess(Cxt,G,Gs) :-
                 hash_create(AttrCount),         
                 convert_tuple_list(G, GL),
                 preprocess_goal(AttrCount, Cxt, GL, GLP, GL2, GLS),
                 append(GLP, GL2, GL3),
                 append(GL3, GLS, GL1),
                 convert_tuple_list(G1, GL1),
                 term_string(G1, Gs).

preprocess_sfop_done(sfget,(sfget,_)).
preprocess_sfop_done(sfset,(_,sfset)).
preprocess_sfop_done(sfattr,(sfget,sfset)).

preprocess_attr(AC, Cxt, F, HGOut, Key, GLP1, GLP, GLS1, GLS):-
            HGOut=..[sfvar, Cnt], 
            (hash_get(AC,Key,(Cnt,SFG,SFS)) ->
                (preprocess_sfop_done(F,(SFG,SFS))->
                    GLP=GLP1, GLS=GLS1; 
                    (F==sfget ->   hash_set(AC, Key, (Cnt,sfget,SFS)),
                                   GLP=[sfget((sfvar(0),Cxt),Key,sfvar(Cnt))|GLP1],GLS=GLS1);
                    (F==sfset ->   hash_set(AC,Key,(Cnt, SFG, sfset)),
                                   GLS=[sfset((sfvar(0),Cxt),Key,sfvar(Cnt))|GLS1],GLP=GLP1);
                    
                    (SFG==sfget -> GLP=GLP1;GLP=[sfget((sfvar(0),Cxt),Key,sfvar(Cnt))|GLP1]),
                    (SFS==sfset -> GLS=GLS1;GLS=[sfset((sfvar(0),Cxt),Key,sfvar(Cnt))|GLS1]),
                    hash_set(AC,Key,(Cnt,sfget,sfset)));
                %%No entry present
                incr_cnt(Cnt), 
                (F==sfget -> hash_set(AC, Key, (Cnt, sfget, nil)),
                             GLP=[sfget((sfvar(0),Cxt),Key, sfvar(Cnt))|GLP1],GLS=GLS1;
                (F==sfset -> hash_set(AC, Key, (Cnt,nil,sfset)),
                             GLS=[sfset((sfvar(0),Cxt),Key, sfvar(Cnt))|GLS1],GLP=GLP1;

                             hash_set(AC, Key, (Cnt,nil,sfset)),
                             GLP=[sfget((sfvar(0),Cxt),Key, sfvar(Cnt))|GLP1],
                             GLS=[sfset((sfvar(0),Cxt),Key, sfvar(Cnt))|GLS1]))).
                                          


preprocess_sfop(sfget).
preprocess_sfop(sfset).
preprocess_sfop(sfattr).

preprocess_attr_try(AC,Cxt,F,HGOut,Args,GLP1,GLP,GLS1,GLS) :- 
        (Args=[Key] -> preprocess_attr(AC, Cxt, F, HGOut, Key, GLP1,
                                       GLP, GLS1, GLS);
                       (Args=[Key,Var] -> GCxt=(sfvar(0),Cxt); Args=[GCxt,Key,Var]),                   
                       preprocess_goal(AC, Cxt, [Var], _, [Var1], _),
                       GLP=GLP1, GLS=GLS1,
                       HGOut=..[F,GCxt,Key,Var1]).

preprocess_goal(_, _, [], [], [], []).
preprocess_goal(AC, Cxt, [HG|TG], GLP, [HGOut|TGOut], GLS) :-
        preprocess_goal(AC, Cxt, TG, GLP1, TGOut, GLS1),
        (var(HG) -> incr_cnt(Cnt), HG=HGOut, HGOut=sfvar(Cnt), GLP=GLP1, GLS=GLS1;
                    HG =..[F|Args],
                    (F==sfcxt ->
                        HGOut=(sfvar(0),Cxt), GLP=GLP1, GLS=GLS1;
                    (F==sfvar ->
                        HGOut=HG, GLP=GLP1, GLS=GLS1;  
                        (preprocess_sfop(F)-> preprocess_attr_try(AC,Cxt,F,HGOut,Args,
                                                GLP1,GLP,GLS1,GLS);
                            preprocess_goal(AC, Cxt, Args, ArgsP, Args1, ArgsS),
                            append(GLP1, ArgsP, GLP), append(GLS1, ArgsS, GLS), HGOut =..[F|Args1])))).


preprocess2(G,Gs) :- 
                 hash_create(AttrCount),         
                 convert_tuple_list(G, GL),
                 preprocess_goal2(AttrCount, GL, GL1),
                 convert_tuple_list(G1, GL1),
                 term_string(G1, Gs).

preprocess_goal2(_, [], []).
preprocess_goal2(AC, [HG|TG], [HGOut|TGOut]) :-
        preprocess_goal2(AC, TG, TGOut),
        (HG = sfvar(Key) ->
            (hash_get(AC, Key, HGOut) -> true;hash_set(AC, Key, HGOut));
            HG=..[F|Args],
            preprocess_goal2(AC, Args, Args1),
            HGOut=..[F|Args1]).

incr_cnt(Cnt) :-
               sfvcnt(Cnt),
               retract(sfvcnt(Cnt)),
               Cnt1 is Cnt + 1,
               assert(sfvcnt(Cnt1)).
        
       
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Description hierarchy state

%1
sfget((SFBinds, Cxt), Attr, Val) :- 
        sfop(SFBinds, Cxt, sfget, Attr, Val).

sfset((SFBinds, Cxt), Attr, Val) :- 
        sfop(SFBinds, Cxt, sfset, Attr, Val).

sfop(SFBinds, Cxt, Op, AttrA, Val) :-
        (atom(AttrA) -> atom_string(AttrA, Attr); AttrA=Attr),
        CDOpL = [Op, Cxt, Attr, Val],
        CDOp =..CDOpL,
        write_exdr(eclipse_to_java, CDOp), 
        flush(eclipse_to_java), 
        read_exdr(java_to_eclipse, ValOut),
        (ValOut=success -> true;
        (ValOut=success(Ref, Val) -> 
            (hash_get(SFBinds, Ref, Val)->true;hash_set(SFBinds,Ref,Val));fail)).

sfop(_,_,_,_,_) :-
        write_exdr(eclipse_to_java, sfundo), 
        flush(eclipse_to_java), 
        read_exdr(java_to_eclipse, _), fail.


% sflocal(Attrs, Pref) :-
%         write_exdr(eclipse_to_java, sflocal(Pref)), 
%         flush(eclipse_to_java), 
%         read_exdr(java_to_eclipse, sflocal(Attrs, Pref)).

% sflocal(Attrs, Pref, Pos) :-
%         write_exdr(eclipse_to_java, sflocal(Pref, Pos)), 
%         flush(eclipse_to_java), 
%         read_exdr(java_to_eclipse, sflocal(Attrs, Pref, Pos)).


