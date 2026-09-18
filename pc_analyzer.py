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
RESULT_COLORS = {'D':'#21c55d','T':'#f59e0b','TIE':'#8b5cf6'}


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
        self.url = tk.StringVar(value='https://yolobet9.com/home')
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
        for text,cmd in [('Import Excel/CSV',self.import_data),('Export Excel/CSV',self.export_data),('Add Result',self.add_result),('Clear All',self.clear_all)]:
            tk.Button(tools,text=text,command=cmd,bg='#374151',fg='white',activebackground='#4b5563',activeforeground='white',font=('Segoe UI',10,'bold'),relief='flat',padx=11,pady=7).pack(side='left',padx=4)
        tk.Label(tools,text='Historical statistical reference only',bg='#111827',fg='#9ca3af',font=('Segoe UI',9)).pack(side='right',padx=10)

        collector = tk.LabelFrame(self.root,text='  YOLOBET9 DATE-WISE RESULT COLLECTOR  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        collector.pack(fill='x',padx=18,pady=6)
        tk.Label(collector,text='Date',bg='#111827',fg='white',font=('Segoe UI',10,'bold')).grid(row=0,column=0,padx=(12,4),pady=9)
        tk.Entry(collector,textvariable=self.collect_date,bg='#1f2937',fg='white',insertbackground='white',width=13,relief='flat').grid(row=0,column=1,padx=4)
        tk.Label(collector,text='Entries',bg='#111827',fg='white',font=('Segoe UI',10,'bold')).grid(row=0,column=2,padx=(14,4))
        ttk.Combobox(collector,textvariable=self.entries,values=[10,25,50,100],state='readonly',width=7).grid(row=0,column=3,padx=4)
        tk.Button(collector,text='PASTE RESULTS',command=self.paste_results,bg='#2563eb',fg='white',font=('Segoe UI',10,'bold'),relief='flat',padx=12,pady=7).grid(row=0,column=4,padx=6)
        tk.Button(collector,text='FETCH PAGE',command=self.fetch_page,bg='#9333ea',fg='white',font=('Segoe UI',10,'bold'),relief='flat',padx=12,pady=7).grid(row=0,column=5,padx=6)
        tk.Button(collector,text='FILTER DATE',command=self.filter_date,bg='#0f766e',fg='white',font=('Segoe UI',10,'bold'),relief='flat',padx=12,pady=7).grid(row=0,column=6,padx=6)
        tk.Label(collector,text='URL',bg='#111827',fg='white',font=('Segoe UI',9,'bold')).grid(row=1,column=0,padx=(12,4),pady=(2,9))
        tk.Entry(collector,textvariable=self.url,bg='#1f2937',fg='white',insertbackground='white',width=75,relief='flat').grid(row=1,column=1,columnspan=6,sticky='ew',padx=4,pady=(2,9))
        tk.Label(collector,textvariable=self.status,bg='#111827',fg='#86efac',font=('Segoe UI',9,'bold')).grid(row=0,column=7,padx=12)

        entry = tk.LabelFrame(self.root,text='  NEW ROUND  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        entry.pack(fill='x',padx=18,pady=6)
        for i,(lab,var,vals) in enumerate([('Dragon',self.dr,CARDS),('Tiger',self.ti,CARDS),('Result',self.re,RESULTS)]):
            tk.Label(entry,text=lab,bg='#111827',fg='#e5e7eb',font=('Segoe UI',10,'bold')).grid(row=0,column=i*2,padx=(12,5),pady=10)
            ttk.Combobox(entry,textvariable=var,values=vals,state='readonly',width=9).grid(row=0,column=i*2+1,padx=5,pady=10)
        tk.Button(entry,text='ADD ROUND',command=self.add_result,bg='#16a34a',fg='white',font=('Segoe UI',10,'bold'),relief='flat',padx=18,pady=7).grid(row=0,column=7,padx=20)

        pair = tk.LabelFrame(self.root,text='  PAIR REFERENCE / STATISTICAL PREDICTION  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        pair.pack(fill='x',padx=18,pady=6)
        tk.Label(pair,text='Pair',bg='#111827',fg='white',font=('Segoe UI',10,'bold')).pack(side='left',padx=(12,5),pady=10)
        ttk.Combobox(pair,textvariable=self.pair,values=[a+b for a in CARDS for b in CARDS],width=12).pack(side='left',padx=5)
        tk.Button(pair,text='ANALYZE',command=self.analyze,bg='#b91c1c',fg='white',font=('Segoe UI',10,'bold'),relief='flat',padx=14,pady=6).pack(side='left',padx=6)
        tk.Label(pair,textvariable=self.summary,bg='#111827',fg='#fde68a',font=('Segoe UI',10,'bold')).pack(side='left',padx=20)
        report = tk.LabelFrame(self.root,text='  OCCURRENCE + PREVIOUS/NEXT 3-ROUND REPORT  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        report.pack(fill='x',padx=18,pady=6)
        report_table = tk.Frame(report,bg='#111827'); report_table.pack(fill='x',padx=8,pady=6)
        rcols=('OCCURRENCE','PREVIOUS 3','MATCH','NEXT 1','NEXT 2','NEXT 3')
        self.report_tree=ttk.Treeview(report_table,columns=rcols,show='headings',height=4)
        rwidths={'OCCURRENCE':90,'PREVIOUS 3':300,'MATCH':90,'NEXT 1':95,'NEXT 2':95,'NEXT 3':95}
        for c in rcols:
            self.report_tree.heading(c,text=c); self.report_tree.column(c,width=rwidths[c],anchor='center')
        self.report_tree.pack(fill='x',expand=True)

        last = tk.LabelFrame(self.root,text='  LAST RESULT  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        last.pack(fill='x',padx=18,pady=6)
        self.last_frame = tk.Frame(last,bg='#111827'); self.last_frame.pack(fill='x',padx=10,pady=7)

        table = tk.Frame(self.root,bg='#111827'); table.pack(fill='both',expand=True,padx=18,pady=(4,12))
        cols=('S NO','ROUND ID','TIME','DRAGON','TIGER','RESULT','PAIR','D O/E','T O/E','PREV RESULT')
        self.tree=ttk.Treeview(table,columns=cols,show='headings')
        widths={'S NO':60,'ROUND ID':155,'TIME':135,'DRAGON':75,'TIGER':75,'RESULT':75,'PAIR':75,'D O/E':85,'T O/E':85,'PREV RESULT':105}
        for c in cols:
            self.tree.heading(c,text=c); self.tree.column(c,width=widths[c],anchor='center')
        for tag,color in [('dragon','#14532d'),('tiger','#92400e'),('tie','#4c1d95')]: self.tree.tag_configure(tag,background=color,foreground='white')
        self.tree.pack(side='left',fill='both',expand=True)
        y=ttk.Scrollbar(table,orient='vertical',command=self.tree.yview); y.pack(side='right',fill='y'); self.tree.configure(yscrollcommand=y.set)

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
        for r in self.data[-14:]:
            tk.Label(self.last_frame,text=r.get('result',''),bg=RESULT_COLORS.get(r.get('result','D'),'#374151'),fg='white',font=('Segoe UI',10,'bold'),width=4,height=2,relief='ridge',bd=1).pack(side='right',padx=3)
        self.update_title_counts()

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
            return
        maxres=max(RESULTS,key=lambda x:cnt[x]); pct=cnt[maxres]/len(rows)*100
        self.summary.set(f'PAIR {p} | CAME {len(rows)} TIMES | D {cnt["D"]} | T {cnt["T"]} | TIE {cnt["TIE"]} | MOST {maxres} ({pct:.1f}%)')
        for idx,r in enumerate(self.data):
            if r.get('dragon','')+r.get('tiger','') != p: continue
            prev=[]
            for j in range(max(0,idx-3),idx):
                q=self.data[j]; prev.append(f'{q.get("result","")} {q.get("dragon","")}{q.get("tiger","")}')
            nxt=[]
            for j in range(idx+1,min(len(self.data),idx+4)):
                q=self.data[j]; nxt.append(f'{q.get("result","")} {q.get("dragon","")}{q.get("tiger","")}')
            while len(nxt)<3: nxt.append('-')
            self.report_tree.insert('', 'end', values=(r.get('sno',''),' | '.join(prev) if prev else '-',f'{r.get("result","")} {p}',nxt[0],nxt[1],nxt[2]),tags=(self.tag_for(r.get('result','D')),))

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

    def fetch_page(self):
        try:
            import urllib.request
            req=urllib.request.Request(self.url.get(),headers={'User-Agent':'Mozilla/5.0'})
            with urllib.request.urlopen(req,timeout=15) as resp: raw=resp.read().decode('utf-8','ignore')
            rows=[]
            for tr in re.findall(r'<tr[^>]*>(.*?)</tr>',raw,re.I|re.S):
                cells=[html.unescape(re.sub('<[^>]+>',' ',c)).strip() for c in re.findall(r'<(?:td|th)[^>]*>(.*?)</(?:td|th)>',tr,re.I|re.S)]
                if cells:
                    rows.append('  '.join(cells))
            incoming=self.parse_pasted_rows('\n'.join(rows))
            if not incoming:
                self.status.set('Page loaded, but no result rows found; site may render results with JavaScript.')
                messagebox.showwarning('Fetch Page','The page loaded, but no result rows were readable. If results are rendered by JavaScript, use PASTE RESULTS after copying the Casino Results table.')
                return
            added=self.merge_collected(incoming)
            self.status.set(f'Date {self.collect_date.get()} | detected {len(incoming)} | added {added}')
            messagebox.showinfo('Fetch Page',f'Detected {len(incoming)} rows. Added {added} new unique Round IDs.')
        except Exception as e:
            self.status.set('Direct fetch unavailable; use PASTE RESULTS')
            messagebox.showwarning('Fetch Page','Direct page fetch was not available. This can happen when the site requires browser JavaScript/login. Copy the date-wise Casino Results rows and use PASTE RESULTS.\n\nDetails: '+str(e))

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
