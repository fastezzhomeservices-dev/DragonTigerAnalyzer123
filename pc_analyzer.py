import tkinter as tk
from tkinter import ttk, filedialog, messagebox
import csv, os
from collections import Counter

CARDS=['A','2','3','4','5','6','7','8','9','10','J','Q','K']
ODD={'A','3','5','7','9','J','K'}

class App:
    def __init__(self, root):
        self.root=root; root.title('Dragon Tiger Analyzer - PC'); root.geometry('1200x760')
        self.data=[]
        top=ttk.Frame(root,padding=8); top.pack(fill='x')
        ttk.Button(top,text='Import Excel/CSV',command=self.import_data).pack(side='left',padx=4)
        ttk.Button(top,text='Export Excel/CSV',command=self.export_data).pack(side='left',padx=4)
        ttk.Button(top,text='Add Result',command=self.add_result).pack(side='left',padx=4)
        ttk.Button(top,text='Clear All',command=self.clear_all).pack(side='left',padx=4)
        form=ttk.LabelFrame(root,text='New Round',padding=8); form.pack(fill='x',padx=8,pady=5)
        self.dr=tk.StringVar(value='A'); self.ti=tk.StringVar(value='A'); self.re=tk.StringVar(value='D')
        for i,(lab,var,vals) in enumerate([('Dragon',self.dr,CARDS),('Tiger',self.ti,CARDS),('Result',self.re,['D','T','TIE'])]):
            ttk.Label(form,text=lab).grid(row=0,column=i*2,padx=5); ttk.Combobox(form,textvariable=var,values=vals,state='readonly',width=8).grid(row=0,column=i*2+1,padx=5)
        search=ttk.LabelFrame(root,text='Pair Reference / Statistical Prediction',padding=8); search.pack(fill='x',padx=8,pady=5)
        self.pair=tk.StringVar(value='JQ'); cb=ttk.Combobox(search,textvariable=self.pair,values=[a+b for a in CARDS for b in CARDS],width=10); cb.pack(side='left',padx=5); cb.bind('<<ComboboxSelected>>',lambda e:self.analyze())
        ttk.Button(search,text='Analyze Pair',command=self.analyze).pack(side='left',padx=5)
        self.summary=tk.StringVar(); ttk.Label(search,textvariable=self.summary,font=('Segoe UI',11,'bold')).pack(side='left',padx=20)
        cols=('S NO','DRAGON','TIGER','RESULT','PAIR','DRAGON O/E','TIGER O/E','PREV RESULT')
        self.tree=ttk.Treeview(root,columns=cols,show='headings'); self.tree.pack(fill='both',expand=True,padx=8,pady=5)
        for c in cols:self.tree.heading(c,text=c);self.tree.column(c,width=120,anchor='center')
        y=ttk.Scrollbar(root,orient='vertical',command=self.tree.yview); y.place(relx=.985,rely=.22,relheight=.70); self.tree.configure(yscrollcommand=y.set)
        self.refresh()
    def oe(self,c): return 'ODD' if c in ODD else 'EVEN'
    def add_result(self):
        n=len(self.data)+1; self.data.append({'sno':n,'dragon':self.dr.get(),'tiger':self.ti.get(),'result':self.re.get()}); self.refresh()
    def clear_all(self):
        if messagebox.askyesno('Confirm','Delete all loaded rounds?'): self.data=[]; self.refresh()
    def refresh(self):
        for x in self.tree.get_children(): self.tree.delete(x)
        for i,r in enumerate(self.data):
            prev=self.data[i-1]['result'] if i else ''
            self.tree.insert('', 'end', values=(r['sno'],r['dragon'],r['tiger'],r['result'],r['dragon']+r['tiger'],self.oe(r['dragon']),self.oe(r['tiger']),prev))
    def analyze(self):
        p=self.pair.get().upper(); rows=[r for r in self.data if r['dragon']+r['tiger']==p]
        cnt=Counter(r['result'] for r in rows); maxres=max(['D','T','TIE'],key=lambda x:cnt[x]) if rows else 'NO HISTORY'
        pct=(cnt[maxres]/len(rows)*100) if rows else 0
        self.summary.set(f'PAIR {p} | MATCH {len(rows)} | D {cnt["D"]} | T {cnt["T"]} | TIE {cnt["TIE"]} | MAX {maxres} ({pct:.1f}%)')
        self.tree.selection_remove(self.tree.selection())
        for item in self.tree.get_children():
            if self.tree.item(item,'values')[4]==p:self.tree.selection_add(item)
    def import_data(self):
        path=filedialog.askopenfilename(filetypes=[('Excel','*.xlsx'),('CSV','*.csv'),('All','*.*')])
        if not path:return
        try:
            rows=[]
            if path.lower().endswith('.csv'):
                with open(path,newline='',encoding='utf-8-sig') as f:
                    for row in csv.reader(f): rows.append(row)
            else:
                from openpyxl import load_workbook
                ws=load_workbook(path,data_only=True).active
                rows=list(ws.iter_rows(values_only=True))
            start=1 if rows and any(str(x).strip().upper() in ('DRAGON','DRIGAN') for x in rows[0]) else 0
            out=[]
            for row in rows[start:]:
                if len(row)<4: continue
                try: sno=int(row[0])
                except: sno=len(out)+1
                d=str(row[1]).strip().upper(); t=str(row[2]).strip().upper(); res=str(row[3]).strip().upper()
                if d in CARDS and t in CARDS and res in ('D','T','TIE'): out.append({'sno':sno,'dragon':d,'tiger':t,'result':res})
            self.data=out; self.refresh(); messagebox.showinfo('Import',f'Imported {len(out)} rounds.')
        except Exception as e: messagebox.showerror('Import error',str(e))
    def export_data(self):
        if not self.data: messagebox.showwarning('Export','No data to export.'); return
        path=filedialog.asksaveasfilename(defaultextension='.xlsx',filetypes=[('Excel','*.xlsx'),('CSV','*.csv')])
        if not path:return
        try:
            rows=[['S NO','DRAGON','TIGER','RESULT','PAIR','DRAGON O/E','TIGER O/E','PREV RESULT']]
            for i,r in enumerate(self.data): rows.append([r['sno'],r['dragon'],r['tiger'],r['result'],r['dragon']+r['tiger'],self.oe(r['dragon']),self.oe(r['tiger']),self.data[i-1]['result'] if i else ''])
            if path.lower().endswith('.csv'):
                with open(path,'w',newline='',encoding='utf-8-sig') as f: csv.writer(f).writerows(rows)
            else:
                from openpyxl import Workbook
                wb=Workbook(); ws=wb.active; ws.title='ANALYZER'
                for row in rows: ws.append(row)
                wb.save(path)
            messagebox.showinfo('Export','Export completed successfully.')
        except Exception as e: messagebox.showerror('Export error',str(e))

if __name__=='__main__':
    root=tk.Tk(); App(root); root.mainloop()
