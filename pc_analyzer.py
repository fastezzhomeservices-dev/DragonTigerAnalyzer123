import tkinter as tk
from tkinter import ttk, filedialog, messagebox
from datetime import datetime
from collections import Counter
import csv
import re
import html

CARDS = ['A','2','3','4','5','6','7','8','9','10','J','Q','K']
ODD = {'A','3','5','7','9','J','K'}
RESULTS = ['D','T','TIE']
RESULT_COLORS = {'D':'#16a34a','T':'#ea580c','TIE':'#7c3aed'}


def clean_card(v):
    v = str(v or '').strip().upper()
    v = re.sub(r'[^A-Z0-9]', '', v)
    if v in CARDS:
        return v
    return ''


def normalize_result(v):
    v = str(v or '').strip().upper()
    if v in ('DRAGON','D'):
        return 'D'
    if v in ('TIGER','T'):
        return 'T'
    if v in ('TIE','DRAW','SAME'):
        return 'TIE'
    return ''


class App:
    def __init__(self, root):
        self.root = root
        root.title('Dragon Tiger Analyzer - PC')
        root.geometry('1320x860')
        root.minsize(1120, 720)
        root.configure(bg='#111827')
        self.data = []
        self.dr = tk.StringVar(value='A')
        self.ti = tk.StringVar(value='A')
        self.re = tk.StringVar(value='D')
        self.pair = tk.StringVar(value='JQ')
        self.summary = tk.StringVar(value='PAIR JQ | NO HISTORY')
        self.collect_date = tk.StringVar(value=datetime.now().strftime('%d/%m/%Y'))
        self.entries = tk.IntVar(value=50)
        self.status = tk.StringVar(value='Ready')
        self.build_styles()
        self.build_ui()
        self.refresh()

    def build_styles(self):
        s = ttk.Style()
        s.theme_use('clam')
        s.configure('TFrame', background='#111827')
        s.configure('TLabelframe', background='#111827', foreground='#e5e7eb')
        s.configure('TLabelframe.Label', background='#111827', foreground='#fbbf24', font=('Segoe UI',11,'bold'))
        s.configure('TLabel', background='#111827', foreground='#f3f4f6', font=('Segoe UI',10))
        s.configure('TButton', font=('Segoe UI',10,'bold'), padding=(11,7), background='#374151', foreground='white')
        s.map('TButton', background=[('active','#4b5563')])
        s.configure('TCombobox', fieldbackground='#1f2937', background='#374151', foreground='#f9fafb', selectbackground='#2563eb', selectforeground='white')
        s.map('TCombobox', fieldbackground=[('readonly','#1f2937')], foreground=[('readonly','#f9fafb')], selectbackground=[('readonly','#2563eb')], selectforeground=[('readonly','white')])
        s.configure('Treeview', background='#1f2937', fieldbackground='#1f2937', foreground='#f9fafb', rowheight=32, font=('Segoe UI',9))
        s.configure('Treeview.Heading', background='#7f1d1d', foreground='white', font=('Segoe UI',9,'bold'), padding=7)

    def build_ui(self):
        header = tk.Frame(self.root, bg='#7f1d1d', height=68)
        header.pack(fill='x')
        for text, bg, width in [('DRAGON','#991b1b',14),('TIE','#312e81',9),('TIGER','#111827',14),('PAIR','#991b1b',14)]:
            tk.Label(header,text=text,bg=bg,fg='white',font=('Segoe UI',16,'bold'),width=width,pady=10).pack(side='left',padx=4,pady=9)
        tk.Label(header,text='PC ANALYZER',bg='#7f1d1d',fg='#fde68a',font=('Segoe UI',15,'bold')).pack(side='right',padx=20)

        tools = tk.Frame(self.root,bg='#111827'); tools.pack(fill='x',padx=18,pady=(11,4))
        for text,cmd in [('Import Excel/CSV',self.import_data),('Export Excel/CSV',self.export_data)]:
            tk.Button(tools,text=text,command=cmd,bg='#374151',fg='white',activebackground='#4b5563',activeforeground='white',font=('Segoe UI',10,'bold'),relief='flat',padx=11,pady=7).pack(side='left',padx=4)
        tk.Label(tools,text='Historical statistical reference only',bg='#111827',fg='#9ca3af',font=('Segoe UI',9)).pack(side='right',padx=10)

        pair = tk.LabelFrame(self.root,text='  PAIR REFERENCE / STATISTICAL PREDICTION  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        pair.pack(fill='x',padx=18,pady=6)
        tk.Label(pair,text='Pair',bg='#111827',fg='white',font=('Segoe UI',10,'bold')).pack(side='left',padx=(12,5),pady=10)
        ttk.Combobox(pair,textvariable=self.pair,values=[a+b for a in CARDS for b in CARDS],width=12).pack(side='left',padx=5)
        tk.Button(pair,text='ANALYZE',command=self.analyze,bg='#b91c1c',fg='white',font=('Segoe UI',10,'bold'),relief='flat',padx=14,pady=6).pack(side='left',padx=6)
        tk.Label(pair,textvariable=self.summary,bg='#111827',fg='#fde68a',font=('Segoe UI',10,'bold')).pack(side='left',padx=20)
        chart_box=tk.LabelFrame(self.root,text='  NUMBER / DRAGON-TIGER FREQUENCY CHART  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        chart_box.pack(fill='x',padx=18,pady=6)
        self.chart_frame=tk.Frame(chart_box,bg='#111827',height=150); self.chart_frame.pack(fill='x',padx=10,pady=8)
        round_box=tk.LabelFrame(self.root,text='  ROUND ANALYSIS GRAPH  ',bg='#111827',fg='#60a5fa',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        round_box.pack(fill='x',padx=18,pady=6)
        self.round_chart=tk.Canvas(round_box,height=180,bg='#111827',highlightthickness=0); self.round_chart.pack(fill='x',padx=10,pady=8)
        report = tk.LabelFrame(self.root,text='  OCCURRENCE + PREVIOUS/NEXT 3-ROUND REPORT  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        report.pack(fill='x',padx=18,pady=6)
        report_table = tk.Frame(report,bg='#111827'); report_table.pack(fill='x',padx=8,pady=6)
        rcols=('OCCURRENCE','PREVIOUS 3','MATCH','NEXT 1','NEXT 2','NEXT 3','NEXT 4','NEXT 5','NEXT 6')
        self.report_tree=ttk.Treeview(report_table,columns=rcols,show='headings',height=6)
        rwidths={'OCCURRENCE':90,'PREVIOUS 3':250,'MATCH':90,'NEXT 1':90,'NEXT 2':90,'NEXT 3':90,'NEXT 4':90,'NEXT 5':90,'NEXT 6':90}
        for c in rcols:
            self.report_tree.heading(c,text=c); self.report_tree.column(c,width=rwidths[c],anchor='center')
        self.report_tree.pack(fill='x',expand=True)

        table = tk.Frame(self.root,bg='#111827'); table.pack(fill='both',expand=True,padx=18,pady=(4,8))
        cols=('S NO','ROUND ID','TIME','DRAGON','TIGER','RESULT','PAIR','D O/E','T O/E','PREV RESULT')
        self.tree=ttk.Treeview(table,columns=cols,show='headings')
        widths={'S NO':60,'ROUND ID':155,'TIME':135,'DRAGON':75,'TIGER':75,'RESULT':75,'PAIR':75,'D O/E':85,'T O/E':85,'PREV RESULT':105}
        for c in cols:
            self.tree.heading(c,text=c); self.tree.column(c,width=widths[c],anchor='center')
        for tag,color in [('dragon','#14532d'),('tiger','#92400e'),('tie','#4c1d95')]: self.tree.tag_configure(tag,background=color,foreground='white')
        self.report_tree.tag_configure('dragon',background='#14532d',foreground='white')
        self.report_tree.tag_configure('tiger',background='#92400e',foreground='white')
        self.report_tree.tag_configure('tie',background='#4c1d95',foreground='white')
        self.tree.pack(side='left',fill='both',expand=True)
        y=ttk.Scrollbar(table,orient='vertical',command=self.tree.yview); y.pack(side='right',fill='y'); self.tree.configure(yscrollcommand=y.set)

        # LAST RESULT stays fixed at the very bottom.
        last = tk.LabelFrame(self.root,text='  LAST RESULT  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        last.pack(fill='x',padx=18,pady=(0,8))
        self.last_frame = tk.Frame(last,bg='#111827',height=82)
        self.last_frame.pack(fill='x',padx=10,pady=6)
        self.last_frame.pack_propagate(False)
        self.last_analysis = tk.StringVar(value='LAST RESULT ANALYSIS | Import rounds to see previous history, card numbers and next historical rounds')
        tk.Label(last,textvariable=self.last_analysis,bg='#111827',fg='#fde68a',font=('Segoe UI',9,'bold'),anchor='w').pack(fill='x',padx=10,pady=(0,5))

    def oe(self,c): return 'ODD' if c in ODD else 'EVEN'
    def tag_for(self,r): return {'D':'dragon','T':'tiger','TIE':'tie'}[r]

    def add_result(self):
        n=max([int(r['sno']) for r in self.data if str(r.get('sno','')).isdigit()] or [0])+1
        self.data.append({'sno':n,'round_id':'','time':'','dragon':self.dr.get(),'tiger':self.ti.get(),'result':self.re.get(),'date':''})
        self.refresh()

    def clear_all(self):
        if messagebox.askyesno('Confirm','Delete all loaded rounds?'):
            self.data=[]; self.refresh()

    def refresh(self, rows=None):
        rows = self.data if rows is None else rows
        for x in self.tree.get_children(): self.tree.delete(x)
        for i,r in enumerate(rows):
            prev = rows[i-1].get('result','') if i else ''
            self.tree.insert('', 'end', values=(r.get('sno',''),r.get('round_id',''),r.get('time',''),r.get('dragon',''),r.get('tiger',''),r.get('result',''),r.get('dragon','')+r.get('tiger',''),self.oe(r.get('dragon','')),self.oe(r.get('tiger','')),prev),tags=(self.tag_for(r.get('result','D')),))
        for x in self.last_frame.winfo_children(): x.destroy()
        recent = self.data[-14:]
        for r in recent:
            res=r.get('result','')
            card=f'{r.get("dragon","")}/{r.get("tiger","")}'
            color=RESULT_COLORS.get(res,'#374151')
            box=tk.Canvas(self.last_frame,width=58,height=78,bg='#111827',highlightthickness=0)
            box.create_oval(5,3,53,51,fill=color,outline='')
            box.create_text(29,27,text=res,fill='white',font=('Segoe UI',11,'bold'))
            box.create_text(29,64,text=card,fill='white',font=('Segoe UI',8,'bold'))
            box.pack(side='right',padx=2)
        self.update_last_analysis()
        self.update_title_counts()

    def update_last_analysis(self):
        if not self.data:
            self.last_analysis.set('LAST RESULT ANALYSIS | No rounds loaded')
            return
        idx=len(self.data)-1
        cur=self.data[idx]
        cur_pair=f'{cur.get("dragon","")}{cur.get("tiger","")}'
        cur_desc=f'{cur.get("result","")} | D:{cur.get("dragon","")} ({self.oe(cur.get("dragon",""))}) | T:{cur.get("tiger","")} ({self.oe(cur.get("tiger",""))}) | Pair:{cur_pair}'
        prev=[]
        for j in range(max(0,idx-5),idx):
            q=self.data[j]
            prev.append(f'{q.get("result","")}[{q.get("dragon","")}/{q.get("tiger","")}]')
        # Historical occurrences of the current result + pair: show what came immediately after each occurrence.
        matches=[]
        for j,r in enumerate(self.data[:-1]):
            if r.get("result")==cur.get("result") and r.get("dragon","")+r.get("tiger","")==cur_pair:
                nxt=self.data[j+1]
                matches.append(f'{nxt.get("result","")} D:{nxt.get("dragon","")} T:{nxt.get("tiger","")}')
        after=' | '.join(matches[-3:]) if matches else 'No previous matching occurrence with a following round'
        self.last_analysis.set(
            f'LAST: {cur_desc}   |   PREVIOUS 5: {"  ".join(prev) if prev else "-"}   |   AFTER SAME RESULT+PAIR: {after}'
        )

    def update_title_counts(self):
        c=Counter(r.get('result') for r in self.data)
        self.summary.set(f'PAIR {self.pair.get().upper()} | D {c["D"]} | T {c["T"]} | TIE {c["TIE"]} | TOTAL {len(self.data)}')

    def analyze(self):
        p=self.pair.get().upper().strip()
        rows=[r for r in self.data if r.get('dragon','')+r.get('tiger','')==p]
        cnt=Counter(r.get('result') for r in rows)
        for x in self.report_tree.get_children(): self.report_tree.delete(x)
        if not rows:
            self.summary.set(f'PAIR {p} | NO HISTORY')
            self.draw_round_graph()
            return
        maxres=max(RESULTS,key=lambda x:cnt[x]); pct=cnt[maxres]/len(rows)*100
        self.summary.set(f'PAIR {p} | CAME {len(rows)} TIMES | D {cnt["D"]} | T {cnt["T"]} | TIE {cnt["TIE"]} | MOST {maxres} ({pct:.1f}%)')
        self.draw_frequency_chart(rows)
        self.draw_round_graph()
        for idx,r in enumerate(self.data):
            if r.get('dragon','')+r.get('tiger','') != p: continue
            prev=[]
            for j in range(max(0,idx-3),idx):
                q=self.data[j]; prev.append(f'{q.get("result","")} {q.get("dragon","")}{q.get("tiger","")}')
            nxt=[]
            for j in range(idx+1,min(len(self.data),idx+7)):
                q=self.data[j]; nxt.append(f'{q.get("result","")} {q.get("dragon","")}{q.get("tiger","")}')
            while len(nxt)<6: nxt.append('-')
            self.report_tree.insert('', 'end', values=(r.get('sno',''),' | '.join(prev) if prev else '-',f'{r.get("result","")} {p}',nxt[0],nxt[1],nxt[2],nxt[3],nxt[4],nxt[5]),tags=(self.tag_for(r.get('result','D')),))

    def draw_frequency_chart(self, rows):
        for w in self.chart_frame.winfo_children(): w.destroy()
        if not rows:
            tk.Label(self.chart_frame,text='No matching pair history',bg='#111827',fg='#9ca3af').pack(pady=25)
            return
        counts=Counter()
        for r in rows:
            d,t=r.get('dragon',''),r.get('tiger','')
            if d: counts[(d,'D')]+=1
            if t: counts[(t,'T')]+=1
        top=sorted(counts.items(),key=lambda x:(-x[1],x[0][0],x[0][1]))[:13]
        maxv=max(v for _,v in top) or 1
        for (num,side),v in top:
            row=tk.Frame(self.chart_frame,bg='#111827'); row.pack(fill='x',pady=2)
            tk.Label(row,text=f'{num}  {side}',bg='#14532d' if side=='D' else '#92400e',fg='white',width=7,font=('Segoe UI',9,'bold')).pack(side='left')
            bar=tk.Canvas(row,height=20,bg='#1f2937',highlightthickness=0); bar.pack(side='left',fill='x',expand=True,padx=6)
            bar.update_idletasks()
            w=max(30,int(bar.winfo_width()*v/maxv))
            bar.create_rectangle(0,2,w,18,fill='#22c55e' if side=='D' else '#f59e0b',outline='')
            tk.Label(row,text=f'{v} times',bg='#111827',fg='white',width=10,anchor='e',font=('Segoe UI',9,'bold')).pack(side='right')

    def draw_round_graph(self):
        cv=self.round_chart
        cv.delete('all')
        data=self.data[-40:]
        if not data:
            cv.create_text(20,80,anchor='w',text='Import data to view round analysis',fill='#9ca3af',font=('Segoe UI',10))
            return
        cv.update_idletasks()
        W=max(cv.winfo_width(),700); H=180
        left=45; right=20; top=18; bottom=38
        usable=max(1,W-left-right)
        step=usable/max(1,len(data))
        barw=max(6,min(18,step*0.65))
        cv.create_line(left,H-bottom,left+usable,H-bottom,fill='#4b5563')
        for i,r in enumerate(data):
            x=left+i*step+step/2
            res=r.get('result','')
            h={'D':85,'T':65,'TIE':45}.get(res,35)
            color={'D':'#22c55e','T':'#f59e0b','TIE':'#a855f7'}.get(res,'#6b7280')
            cv.create_rectangle(x-barw/2,H-bottom-h,x+barw/2,H-bottom,fill=color,outline='')
            if len(data)<=20 or i%2==0:
                cv.create_text(x,H-bottom-h-9,text=res,fill='white',font=('Segoe UI',8,'bold'))
            label=str(r.get('sno',''))
            if len(data)<=25 or i%4==0:
                cv.create_text(x,H-bottom+12,text=label,fill='#9ca3af',font=('Segoe UI',8))
            num=f"{r.get('dragon','')}/{r.get('tiger','')}"
            if len(data)<=20 or i%3==0:
                cv.create_text(x,H-bottom+27,text=num,fill='#d1d5db',font=('Segoe UI',7))
        cv.create_text(left,8,anchor='w',text='Latest 40 rounds | bar = result | label = D/T card numbers',fill='#cbd5e1',font=('Segoe UI',9,'bold'))

    def import_data(self):
        path=filedialog.askopenfilename(filetypes=[('Excel','*.xlsx'),('CSV','*.csv'),('All files','*.*')])
        if not path:return
        try:
            rows=[]
            if path.lower().endswith('.csv'):
                with open(path,newline='',encoding='utf-8-sig') as f: rows=list(csv.reader(f))
            else:
                from openpyxl import load_workbook
                wb=load_workbook(path,data_only=True); ws=wb.active; rows=list(ws.iter_rows(values_only=True))
            out=[]; seen=set()
            for row in rows:
                if len(row)<4: continue
                try: sno=int(row[0])
                except: continue
                d=clean_card(row[1]); t=clean_card(row[2]); res=normalize_result(row[3])
                if d and t and res and sno not in seen:
                    out.append({'sno':sno,'round_id':str(row[4]) if len(row)>4 else '','time':str(row[5]) if len(row)>5 else '','dragon':d,'tiger':t,'result':res,'date':str(row[6]) if len(row)>6 else ''}); seen.add(sno)
            out.sort(key=lambda r:r['sno']); self.data=out; self.refresh(); messagebox.showinfo('Import',f'Imported {len(out)} unique rounds.')
        except Exception as e: messagebox.showerror('Import error',str(e))

    def export_data(self):
        if not self.data: messagebox.showwarning('Export','No data to export.'); return
        path=filedialog.asksaveasfilename(defaultextension='.xlsx',filetypes=[('Excel','*.xlsx'),('CSV','*.csv')])
        if not path:return
        try:
            rows=[['S NO','ROUND ID','TIME','DRAGON','TIGER','RESULT','PAIR','DRAGON O/E','TIGER O/E','DATE']]
            for r in self.data: rows.append([r.get('sno',''),r.get('round_id',''),r.get('time',''),r.get('dragon',''),r.get('tiger',''),r.get('result',''),r.get('dragon','')+r.get('tiger',''),self.oe(r.get('dragon','')),self.oe(r.get('tiger','')),r.get('date','')])
            if path.lower().endswith('.csv'):
                with open(path,'w',newline='',encoding='utf-8-sig') as f: csv.writer(f).writerows(rows)
            else:
                from openpyxl import Workbook
                wb=Workbook(); ws=wb.active; ws.title='ANALYZER'
                for row in rows: ws.append(row)
                wb.save(path)
            messagebox.showinfo('Export','Export completed successfully.')
        except Exception as e: messagebox.showerror('Export error',str(e))

    def parse_pasted_rows(self, text):
        lines=[x.strip() for x in text.replace('\r','').split('\n') if x.strip()]
        out=[]
        for line in lines:
            parts=re.split(r'\t|\s{2,}|\|',line)
            joined=' '.join(parts)
            if re.search(r'round\s*id|match\s*time|winner',joined,re.I):
                continue
            rid=''
            m=re.search(r'\b(\d{10,20})\b',line)
            if m: rid=m.group(1)
            res=normalize_result(re.search(r'\b(DRAGON|TIGER|TIE|DRAW)\b',line,re.I).group(1)) if re.search(r'\b(DRAGON|TIGER|TIE|DRAW)\b',line,re.I) else ''
            cards=[]
            for token in re.findall(r'(?<![A-Z0-9])(10|[2-9AJQK])(?![A-Z0-9])',line.upper()): cards.append(token)
            if rid and res:
                d=cards[0] if len(cards)>=2 else ''
                t=cards[1] if len(cards)>=2 else ''
                out.append({'round_id':rid,'time':'','dragon':d,'tiger':t,'result':res,'date':self.collect_date.get()})
        return out

    def merge_collected(self, incoming):
        if not incoming: return 0
        existing_ids={str(r.get('round_id','')) for r in self.data if r.get('round_id')}
        next_sno=max([int(r['sno']) for r in self.data if str(r.get('sno','')).isdigit()] or [0])+1
        added=0
        limit=max(1,int(self.entries.get()))
        for r in incoming[:limit]:
            rid=str(r.get('round_id','')).strip()
            if not rid or rid in existing_ids: continue
            r['sno']=next_sno; next_sno+=1; self.data.append(r); existing_ids.add(rid); added+=1
        self.data.sort(key=lambda x:int(x.get('sno',0)) if str(x.get('sno','')).isdigit() else 0)
        self.refresh()
        return added

    def paste_results(self):
        try:
            text=self.root.clipboard_get()
        except Exception:
            messagebox.showwarning('Paste Results','Copy the YoloBet9 Casino Results rows first, then click PASTE RESULTS.')
            return
        incoming=self.parse_pasted_rows(text)
        if not incoming:
            messagebox.showwarning('Paste Results','No Round ID + Winner rows were detected. Copy the result table rows and try again.')
            return
        added=self.merge_collected(incoming)
        self.status.set(f'Date {self.collect_date.get()} | detected {len(incoming)} | added {added} | duplicates skipped')
        messagebox.showinfo('YoloBet9 Import',f'Detected {len(incoming)} results. Added {added} new unique Round IDs. Duplicate Round IDs were skipped.')

    def filter_date(self):
        target=self.collect_date.get().strip()
        rows=[]
        for r in self.data:
            d=str(r.get('date','')).strip()
            if d==target: rows.append(r)
            elif not d and str(r.get('time','')).startswith(target): rows.append(r)
        self.refresh(rows)
        self.status.set(f'Date filter {target} | showing {len(rows)} stored rounds')


if __name__=='__main__':
    root=tk.Tk(); App(root); root.mainloop()
